# tts_helper.py
import sys
from gtts import gTTS
from playsound import playsound
import tempfile
import os

def speak(text):
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix='.mp3') as f:
            temp_file = f.name
        
        # Generate suara Indonesia (wanita, natural)
        tts = gTTS(text=text, lang='id', slow=False)
        tts.save(temp_file)
        playsound(temp_file)
        os.unlink(temp_file)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        speak(" ".join(sys.argv[1:]))