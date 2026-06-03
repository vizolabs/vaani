"""
Helper script to download and verify NLLB-200-distilled-600M ONNX files
for the Vaani keyboard AI translation feature.

Usage:
    python scripts/prepare_model.py [output_dir]
    
Default output_dir: ./model
"""

import hashlib
import os
import sys
import urllib.request

FILES = {
    "encoder_model_quantized.onnx": {
        "url": "https://huggingface.co/Xenova/nllb-200-distilled-600M/resolve/main/onnx/encoder_model_quantized.onnx",
        "size": 419_000_000,
    },
    "decoder_with_past_model_quantized.onnx": {
        "url": "https://huggingface.co/Xenova/nllb-200-distilled-600M/resolve/main/onnx/decoder_with_past_model_quantized.onnx",
        "size": 445_000_000,
    },
    "sentencepiece.bpe.model": {
        "url": "https://huggingface.co/facebook/nllb-200-distilled-600M/resolve/main/sentencepiece.bpe.model",
        "size": 4_000_000,
    },
}


def download_file(url: str, dest: str, expected_size: int) -> bool:
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        print(f"  [SKIP] {os.path.basename(dest)} already exists")
        return True
    try:
        print(f"  [DOWNLOAD] {os.path.basename(dest)} ({expected_size // 1_000_000} MB)")
        urllib.request.urlretrieve(url, dest)
        actual_size = os.path.getsize(dest)
        if actual_size == 0:
            print(f"  [ERROR] Downloaded file is empty")
            return False
        print(f"  [OK] {actual_size:,} bytes downloaded")
        return True
    except Exception as e:
        print(f"  [ERROR] {e}")
        return False


def main():
    output_dir = sys.argv[1] if len(sys.argv) > 1 else "model"
    os.makedirs(output_dir, exist_ok=True)

    total_size = sum(f["size"] for f in FILES.values())
    print(f"Model directory: {output_dir}")
    print(f"Total download size: ~{total_size // 1_000_000} MB")
    print()

    all_ok = True
    for name, info in FILES.items():
        dest = os.path.join(output_dir, name)
        if not download_file(info["url"], dest, info["size"]):
            all_ok = False

    print()
    if all_ok:
        print("All files downloaded successfully.")
        print(f"Copy the contents of '{output_dir}' to your Android device's")
        print("model directory (typically Android/data/com.vaani.keyboard/files/models/).")
    else:
        print("Some downloads failed. Check errors above.")
        sys.exit(1)


if __name__ == "__main__":
    main()
