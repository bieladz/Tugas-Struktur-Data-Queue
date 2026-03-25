import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;


class Node {
    int nomor;
    String nama;
    Node next;  

    Node(int nomor, String nama) {
        this.nomor = nomor;
        this.nama = nama;
        this.next = null;
    }
}

class QueueLinkedList {
    private Node head; 
    private Node tail;  
    private int size;
    private int counter; 

    public QueueLinkedList() {
        head = null;
        tail = null;
        size = 0;
        counter = 1;
    }

    public boolean isEmpty() {
        return head == null;
    }

    /** Tambah antrian baru (enqueue), return nomor antrian */
    public int enqueue(String nama) {
        int nomor = counter++;
        Node newNode = new Node(nomor, nama);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
        return nomor;
    }

    /** Panggil & hapus antrian terdepan (dequeue) */
    public Node dequeue() {
        if (isEmpty()) return null;
        Node node = head;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return node;
    }

    /** Lihat antrian terdepan tanpa menghapus */
    public Node peek() {
        return head;
    }

    public int getSize() {
        return size;
    }

    /** Kembalikan semua antrian sebagai List */
    public List<int[]> toList() {
        List<int[]> list = new ArrayList<>();
        Node current = head;
        while (current != null) {
            list.add(new int[]{current.nomor});
            current = current.next;
        }
        return list;
    }

    public List<Node> toNodeList() {
        List<Node> list = new ArrayList<>();
        Node current = head;
        while (current != null) {
            list.add(current);
            current = current.next;
        }
        return list;
    }
}

class TextToSpeech {
    
    private static final String PYTHON_CMD = "python"; 
    private static final String TTS_SCRIPT = "tts_helper.py";
    
    public static void speak(final String text) {
        new Thread(() -> {
            try {
                String safeText = text.replace("'", "'\\''").replace("\"", "\\\"");
                
                ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_CMD, TTS_SCRIPT, safeText
                );
                pb.redirectErrorStream(true);
                Process p = pb.start();
                
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
                System.err.println("[TTS Error] " + e.getMessage());
            }
        }).start();
    }
}

class RoundedPanel extends JPanel {
    private int arc;
    private Color bg;

    RoundedPanel(int arc, Color bg) {
        this.arc = arc;
        this.bg = bg;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }
}

public class PosyanduAntrian extends JFrame {

    // ── Warna ──
    static final Color C_BG       = new Color(0xF8F8E1);
    static final Color C_HEADER   = new Color(0x008080);
    static final Color C_ACCENT   = new Color(0x4988C4);
    static final Color C_GREEN    = new Color(0x03A791);
    static final Color C_PINK     = new Color(0xEB4C4C);
    static final Color C_RED      = new Color(0xDB1A1A);
    static final Color C_YELLOW   = new Color(0xFFF9C4);
    static final Color C_ROW_EVEN = new Color(0xFFA6A6);
    static final Color C_ROW_ODD  = Color.WHITE;
    static final Color C_TEXT     = new Color(0x134E8E);
    static final Color C_SUBTEXT  = new Color(0x546E7A);

    private QueueLinkedList queue = new QueueLinkedList();

    private JLabel lblCalled;
    private JPanel bannerPanel;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblTotal;
    private JLabel statusBar;

    public PosyanduAntrian() {
        super("🤱 Sistem Antrian Posyandu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── HEADER ──
    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());

        // Top header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_HEADER);
        header.setPreferredSize(new Dimension(860, 70));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("🤱  SISTEM ANTRIAN POSYANDU");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        lblTotal = new JLabel("Antrian: 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTotal.setForeground(new Color(0xFDFFFF));
        header.add(lblTotal, BorderLayout.EAST);

        // Called banner
        bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(C_YELLOW);
        bannerPanel.setPreferredSize(new Dimension(860, 58));
        bannerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        lblCalled = new JLabel("— Belum ada antrian yang dipanggil —", SwingConstants.CENTER);
        lblCalled.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCalled.setForeground(C_RED);
        bannerPanel.add(lblCalled, BorderLayout.CENTER);

        wrap.add(header, BorderLayout.NORTH);
        wrap.add(bannerPanel, BorderLayout.SOUTH);
        return wrap;
    }

    // ── CENTER: Buttons + Table ──
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(C_BG);
        center.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(C_BG);
        btnRow.setBorder(new EmptyBorder(8, 0, 12, 0));

        btnRow.add(buildBtn("➕  Ambil Antrian",    C_GREEN,  e -> ambilAntrian()));
        btnRow.add(buildBtn("🗒️  Tampilkan Antrian", C_ACCENT, e -> tampilkanAntrian()));
        btnRow.add(buildBtn("🔔  Panggil Antrian",   C_PINK,    e -> panggilAntrian()));

        JPanel rightBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtn.setBackground(C_BG);
        rightBtn.add(buildBtn("🗑  Reset", new Color(0xBF360C), e -> reset()));

        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setBackground(C_BG);
        btnWrap.add(btnRow, BorderLayout.WEST);
        btnWrap.add(rightBtn, BorderLayout.EAST);

        center.add(btnWrap, BorderLayout.NORTH);
        center.add(buildTable(), BorderLayout.CENTER);
        return center;
    }

    private JButton buildBtn(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 38));
        btn.addActionListener(al);
        return btn;
    }

    // ── TABLE ──
    private JScrollPane buildTable() {
        String[] cols = {"No. Antrian", "Nama Pasien", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setBackground(C_ACCENT); 
        headerRenderer.setForeground(Color.WHITE); 
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        table.setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setSelectionBackground(new Color(0xBBDEFB));
        table.setForeground(C_TEXT);

        // Header style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(C_ACCENT);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Center renderer
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(col == 1 ? LEFT : CENTER);
                setBackground(row % 2 == 0 ? C_ROW_EVEN : C_ROW_ODD);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        };
        for (int i = 0; i < 3; i++) table.getColumnModel().getColumn(i).setCellRenderer(centerR);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xBBDEFB), 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ── STATUS BAR ──
    private JLabel buildStatusBar() {
        statusBar = new JLabel("  Selamat Datang di Posyandu.");
        statusBar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
        statusBar.setForeground(Color.WHITE);
        statusBar.setBackground(C_HEADER);
        statusBar.setOpaque(true);
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        return statusBar;
    }

    private void ambilAntrian() {
        String nama = JOptionPane.showInputDialog(this,
                "Masukkan nama pasien:", "Ambil Antrian",
                JOptionPane.QUESTION_MESSAGE);
        if (nama == null) return;
        nama = nama.trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nama tidak boleh kosong!", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int nomor = queue.enqueue(nama);
        refreshTable();
        setStatus("✅ Antrian No. " + String.format("%03d", nomor) + " atas nama '" + nama + "' ditambahkan.");

        String pesan = String.format(
        "<html>" +
        "<div style='text-align: center; width: 200px;'>" +
        "Nomor Antrian Anda:<br><br>" +
        "<span style='font-size:24px'><b>🎫 %03d</b></span><br><br>" +
        "👤 %s<br><br>" +
        "<i>Silakan menunggu.</i>" +
        "</div>" +
        "</html>", nomor, nama);
        JOptionPane.showMessageDialog(this, pesan, "Antrian Diterima", JOptionPane.INFORMATION_MESSAGE);
        TextToSpeech.speak("Nomor antrian " + nomor + ", atas nama " + nama + ", telah terdaftar.");
    }

    private void tampilkanAntrian() {
        refreshTable();
        int n = queue.getSize();
        if (n == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada antrian saat ini.",
                    "Antrian Kosong", JOptionPane.INFORMATION_MESSAGE);
            setStatus("ℹ️ Antrian kosong.");
        } else {
            setStatus("📋 Menampilkan " + n + " antrian.");
        }
    }

    private void panggilAntrian() {
        if (queue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada antrian yang bisa dipanggil.",
                    "Antrian Kosong", JOptionPane.INFORMATION_MESSAGE);
            setStatus("⚠️ Antrian kosong, tidak ada yang dipanggil.");
            return;
        }
        Node node = queue.dequeue();
        String teks = String.format("🔔  Memanggil:   No. %03d  —  %s", node.nomor, node.nama.toUpperCase());
        lblCalled.setText(teks);
        lblCalled.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));

        refreshTable();

        setStatus(String.format("🔔 Memanggil antrian No. %03d atas nama '%s'.", node.nomor, node.nama));

        String pesan = "Nomor antrian " + node.nomor + ", atas nama " + node.nama + ", silakan menuju ruang pemeriksaan.";
        TextToSpeech.speak(pesan);
        
        flashBanner();
    }

    private void reset() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Reset semua antrian?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;
        queue = new QueueLinkedList();
        tableModel.setRowCount(0);
        lblTotal.setText("Antrian: 0");
        lblCalled.setText("— Belum ada antrian yang dipanggil —");
        lblCalled.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 13));
        bannerPanel.setBackground(C_YELLOW);
        lblCalled.setBackground(C_YELLOW);
        setStatus("🗑 Semua antrian telah direset.");
        lblCalled.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 13));
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        List<Node> nodes = queue.toNodeList();
        for (Node n : nodes) {
            tableModel.addRow(new Object[]{
                String.format("%03d", n.nomor), n.nama, "⏳ Menunggu"
            });
        }
        lblTotal.setText("Antrian: " + queue.getSize());
    }

    private void setStatus(String msg) {
        statusBar.setText("  " + msg);
    }

    private void flashBanner() {
        Color[] colors = {new Color(0xFF6F00), C_YELLOW, new Color(0xFF6F00), C_YELLOW, C_YELLOW};
        Timer[] timerHolder = new Timer[1];
        int[] step = {0};
        timerHolder[0] = new Timer(200, e -> {
            if (step[0] < colors.length) {
                bannerPanel.setBackground(colors[step[0]]);
                lblCalled.setBackground(colors[step[0]]);
                step[0]++;
            } else {
                timerHolder[0].stop();
            }
        });
        timerHolder[0].start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(PosyanduAntrian::new);
    }
}