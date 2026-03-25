"""
Simulasi Antrian Posyandu - Python GUI
Menggunakan Linked List sebagai struktur Queue
Fitur: Ambil Antrian, Tampilkan Antrian, Panggil Antrian (dengan suara)
"""

import tkinter as tk
from tkinter import ttk, messagebox, simpledialog
import threading
import time

# ─────────────────────────────────────────────
# LINKED LIST NODE & QUEUE
# ─────────────────────────────────────────────

class Node:
    """Node untuk Linked List"""
    def __init__(self, nomor: int, nama: str):
        self.nomor = nomor
        self.nama = nama
        self.next = None  # pointer ke node berikutnya

class QueueLinkedList:
    """Queue berbasis Linked List (FIFO)"""
    def __init__(self):
        self.head = None   # front of queue
        self.tail = None   # rear of queue
        self.size = 0
        self.counter = 1   # auto-increment nomor antrian

    def is_empty(self) -> bool:
        return self.head is None

    def enqueue(self, nama: str) -> int:
        """Tambah antrian baru, kembalikan nomor antrian"""
        nomor = self.counter
        self.counter += 1
        new_node = Node(nomor, nama)
        if self.tail is None:
            self.head = new_node
            self.tail = new_node
        else:
            self.tail.next = new_node
            self.tail = new_node
        self.size += 1
        return nomor

    def dequeue(self):
        """Hapus & kembalikan node terdepan (panggil antrian)"""
        if self.is_empty():
            return None
        node = self.head
        self.head = self.head.next
        if self.head is None:
            self.tail = None
        self.size -= 1
        return node

    def peek(self):
        """Lihat antrian terdepan tanpa menghapus"""
        return self.head

    def to_list(self) -> list:
        """Kembalikan semua antrian sebagai list of (nomor, nama)"""
        result = []
        current = self.head
        while current:
            result.append((current.nomor, current.nama))
            current = current.next
        return result

# ─────────────────────────────────────────────
# TEXT-TO-SPEECH (opsional, graceful fallback)
# ─────────────────────────────────────────────

def speak(text: str):
    """Panggil suara menggunakan pyttsx3 jika tersedia, atau print fallback"""
    def _speak():
        try:
            import pyttsx3
            engine = pyttsx3.init()
            engine.setProperty('rate', 130)
            # Coba set bahasa Indonesia jika tersedia
            voices = engine.getProperty('voices')
            for v in voices:
                if 'indonesia' in v.name.lower() or 'id' in v.id.lower():
                    engine.setProperty('voice', v.id)
                    break
            engine.say(text)
            engine.runAndWait()
            engine.stop()
        except ImportError:
            print(f"[TTS] {text}")
        except Exception as e:
            print(f"[TTS Error] {e}")
    threading.Thread(target=_speak, daemon=True).start()

# ─────────────────────────────────────────────
# MAIN APPLICATION GUI
# ─────────────────────────────────────────────

class PosyanduApp:
    COLORS = {
        'bg':         '#F0F7FF',
        'header_bg':  '#1565C0',
        'header_fg':  '#FFFFFF',
        'card_bg':    '#FFFFFF',
        'accent':     '#1976D2',
        'accent2':    '#43A047',
        'danger':     '#E53935',
        'warn':       '#FB8C00',
        'text':       '#1A237E',
        'subtext':    '#546E7A',
        'row_even':   '#E3F2FD',
        'row_odd':    '#FFFFFF',
        'border':     '#BBDEFB',
        'called_bg':  '#FFF9C4',
        'called_fg':  '#E65100',
    }

    def __init__(self, root: tk.Tk):
        self.root = root
        self.queue = QueueLinkedList()
        self.last_called = None
        self._setup_root()
        self._build_ui()

    def _setup_root(self):
        self.root.title("🏥 Sistem Antrian Posyandu")
        self.root.geometry("820x680")
        self.root.resizable(False, False)
        self.root.configure(bg=self.COLORS['bg'])
        self.root.eval('tk::PlaceWindow . center')

    def _build_ui(self):
        c = self.COLORS

        # ── HEADER ──
        header = tk.Frame(self.root, bg=c['header_bg'], height=80)
        header.pack(fill='x')
        header.pack_propagate(False)
        tk.Label(header, text="🏥  SISTEM ANTRIAN POSYANDU",
                 font=('Segoe UI', 18, 'bold'), bg=c['header_bg'],
                 fg=c['header_fg']).pack(side='left', padx=20, pady=18)
        self.lbl_total = tk.Label(header, text="Antrian: 0",
                                  font=('Segoe UI', 11), bg=c['header_bg'],
                                  fg='#BBDEFB')
        self.lbl_total.pack(side='right', padx=20)

        # ── CALLED BANNER ──
        self.banner = tk.Frame(self.root, bg=c['called_bg'], height=64)
        self.banner.pack(fill='x')
        self.banner.pack_propagate(False)
        self.lbl_called = tk.Label(self.banner,
                                   text="— Belum ada antrian yang dipanggil —",
                                   font=('Segoe UI', 13, 'italic'),
                                   bg=c['called_bg'], fg=c['called_fg'])
        self.lbl_called.pack(expand=True)

        # ── BUTTON ROW ──
        btn_row = tk.Frame(self.root, bg=c['bg'], pady=14)
        btn_row.pack(fill='x', padx=20)

        self._btn(btn_row, "➕  Ambil Antrian",  c['accent2'], self._ambil_antrian, side='left')
        self._btn(btn_row, "📋  Tampilkan Antrian", c['accent'], self._tampilkan_antrian, side='left')
        self._btn(btn_row, "📣  Panggil Antrian", c['danger'], self._panggil_antrian, side='left')
        self._btn(btn_row, "🗑  Reset", c['warn'], self._reset, side='right')

        # ── TABLE ──
        table_frame = tk.Frame(self.root, bg=c['bg'], padx=20)
        table_frame.pack(fill='both', expand=True)

        tk.Label(table_frame, text="📋  Daftar Antrian",
                 font=('Segoe UI', 11, 'bold'),
                 bg=c['bg'], fg=c['text']).pack(anchor='w', pady=(0, 4))

        cols = ('no_antrian', 'nama', 'status')
        self.tree = ttk.Treeview(table_frame, columns=cols,
                                  show='headings', height=14)

        style = ttk.Style()
        style.theme_use('clam')
        style.configure('Treeview',
                        background=c['card_bg'], foreground=c['text'],
                        rowheight=32, font=('Segoe UI', 10),
                        fieldbackground=c['card_bg'], borderwidth=0)
        style.configure('Treeview.Heading',
                        background=c['accent'], foreground='white',
                        font=('Segoe UI', 10, 'bold'), relief='flat')
        style.map('Treeview', background=[('selected', '#BBDEFB')])

        self.tree.tag_configure('even', background=c['row_even'])
        self.tree.tag_configure('odd',  background=c['row_odd'])
        self.tree.tag_configure('waiting', foreground='#1565C0')
        self.tree.tag_configure('called', foreground='#E53935', font=('Segoe UI', 10, 'bold'))

        self.tree.heading('no_antrian', text='No. Antrian')
        self.tree.heading('nama',       text='Nama Pasien')
        self.tree.heading('status',     text='Status')
        self.tree.column('no_antrian', width=120, anchor='center')
        self.tree.column('nama',       width=380, anchor='w')
        self.tree.column('status',     width=160, anchor='center')

        sb = ttk.Scrollbar(table_frame, orient='vertical', command=self.tree.yview)
        self.tree.configure(yscroll=sb.set)
        self.tree.pack(side='left', fill='both', expand=True)
        sb.pack(side='right', fill='y')

        # ── STATUS BAR ──
        self.statusbar = tk.Label(self.root, text="Selamat datang di Posyandu.",
                                  font=('Segoe UI', 9), bg='#1565C0',
                                  fg='white', anchor='w', padx=10, pady=4)
        self.statusbar.pack(fill='x', side='bottom')

    def _btn(self, parent, text, color, cmd, side='left'):
        b = tk.Button(parent, text=text, command=cmd,
                      bg=color, fg='white', activebackground=color,
                      font=('Segoe UI', 10, 'bold'), relief='flat',
                      cursor='hand2', padx=14, pady=8, bd=0)
        b.pack(side=side, padx=6)
        b.bind('<Enter>', lambda e: b.configure(bg=self._darken(color)))
        b.bind('<Leave>', lambda e: b.configure(bg=color))
        return b

    def _darken(self, hex_color: str) -> str:
        r = max(0, int(hex_color[1:3], 16) - 30)
        g = max(0, int(hex_color[3:5], 16) - 30)
        b = max(0, int(hex_color[5:7], 16) - 30)
        return f'#{r:02X}{g:02X}{b:02X}'

    # ── ACTIONS ──

    def _ambil_antrian(self):
        nama = simpledialog.askstring("Ambil Antrian",
                                      "Masukkan nama pasien:",
                                      parent=self.root)
        if nama is None:
            return
        nama = nama.strip()
        if not nama:
            messagebox.showwarning("Input Kosong", "Nama tidak boleh kosong!", parent=self.root)
            return
        nomor = self.queue.enqueue(nama)
        self._refresh_table()
        self._set_status(f"✅ Antrian No. {nomor:03d} atas nama '{nama}' berhasil ditambahkan.")
        messagebox.showinfo("Antrian Diterima",
                            f"Nomor Antrian Anda:\n\n🎫  {nomor:03d}\n\n👤  {nama}\n\nSilakan menunggu.",
                            parent=self.root)
        speak(f"Nomor antrian {nomor}, atas nama {nama}, telah terdaftar.")

    def _tampilkan_antrian(self):
        self._refresh_table()
        n = self.queue.size
        if n == 0:
            self._set_status("ℹ️ Antrian kosong.")
            messagebox.showinfo("Antrian Kosong",
                                "Tidak ada antrian saat ini.", parent=self.root)
        else:
            self._set_status(f"📋 Menampilkan {n} antrian.")

    def _panggil_antrian(self):
        if self.queue.is_empty():
            messagebox.showinfo("Antrian Kosong",
                                "Tidak ada antrian yang bisa dipanggil.", parent=self.root)
            self._set_status("⚠️ Antrian kosong, tidak ada yang dipanggil.")
            return
        node = self.queue.dequeue()
        self.last_called = (node.nomor, node.nama)

        teks = f"Nomor {node.nomor:03d}  —  {node.nama.upper()}"
        self.lbl_called.config(
            text=f"📣  Memanggil:   {teks}",
            font=('Segoe UI', 14, 'bold'))

        self._refresh_table()
        self._set_status(f"📣 Memanggil antrian No. {node.nomor:03d} atas nama '{node.nama}'.")

        # Suara panggilan
        pesan_suara = (f"Nomor antrian {node.nomor}, "
                       f"atas nama {node.nama}, "
                       f"silakan menuju loket pemeriksaan.")
        speak(pesan_suara)

        # Animasi banner
        self._flash_banner()

    def _reset(self):
        if not messagebox.askyesno("Konfirmasi", "Reset semua antrian?", parent=self.root):
            return
        self.queue = QueueLinkedList()
        self.last_called = None
        self.lbl_called.config(
            text="— Belum ada antrian yang dipanggil —",
            font=('Segoe UI', 13, 'italic'))
        self._refresh_table()
        self._set_status("🗑 Semua antrian telah direset.")

    # ── HELPERS ──

    def _refresh_table(self):
        for row in self.tree.get_children():
            self.tree.delete(row)
        items = self.queue.to_list()
        for i, (nomor, nama) in enumerate(items):
            tag_row = 'even' if i % 2 == 0 else 'odd'
            self.tree.insert('', 'end',
                             values=(f"{nomor:03d}", nama, "⏳ Menunggu"),
                             tags=(tag_row, 'waiting'))
        self.lbl_total.config(text=f"Antrian: {self.queue.size}")

    def _set_status(self, msg: str):
        self.statusbar.config(text=f"  {msg}")

    def _flash_banner(self):
        colors = ['#FF6F00', '#FFF9C4', '#FF6F00', '#FFF9C4', '#FFF9C4']
        def _step(i=0):
            if i < len(colors):
                self.banner.config(bg=colors[i])
                self.lbl_called.config(bg=colors[i])
                self.root.after(200, lambda: _step(i + 1))
        _step()

# ─────────────────────────────────────────────
# ENTRY POINT
# ─────────────────────────────────────────────

if __name__ == '__main__':
    root = tk.Tk()
    app = PosyanduApp(root)
    root.mainloop()