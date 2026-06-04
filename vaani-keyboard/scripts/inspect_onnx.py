"""
Inspect ONNX model files to dump input/output tensor names and shapes.
Useful for verifying tensor name assumptions in NllbTranslator.kt.

Usage:
    python scripts/inspect_onnx.py path/to/model.onnx [path/to/decoder.onnx]
"""

import sys

try:
    import onnx
except ImportError:
    print("Requires onnx library. Install with: pip install onnx")
    sys.exit(1)


def inspect(path: str):
    print(f"\n{'='*60}")
    print(f"Model: {path}")
    print(f"{'='*60}")
    model = onnx.load(path)
    graph = model.graph

    print(f"\n--- Inputs ({len(graph.input)}) ---")
    for inp in graph.input:
        shape = [d.dim_value for d in inp.type.tensor_type.shape.dim]
        print(f"  {inp.name}: shape={shape}")

    print(f"\n--- Outputs ({len(graph.output)}) ---")
    for out in graph.output:
        shape = [d.dim_value for d in out.type.tensor_type.shape.dim]
        print(f"  {out.name}: shape={shape}")

    print(f"\n--- Initializers ({len(graph.initializer)}) ---")
    for init in graph.initializer:
        print(f"  {init.name}: shape={list(init.dims)}")


def main():
    paths = sys.argv[1:]
    if not paths:
        print("Usage: python scripts/inspect_onnx.py path/to/model.onnx [path/to/decoder.onnx]")
        sys.exit(1)
    for path in paths:
        inspect(path)


if __name__ == "__main__":
    main()
