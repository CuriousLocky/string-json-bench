# How to build and run

- Download/Compile GraalVM and set evironment value `GRAALVM_HOME`
- Checkout to a branch
    - `master`: default json parser with java string
    - `baseline-noswith`: with java string but replaced switch expression with if blocks
    - `asrstring`: json parser with customized string representation
    - `byte-cache`: customized string representation with byte-string cache optimization
- Build binary with `build-native.sh`
- Run the benchmark with the binary `harness` under `out`
    - Usage: `harness Json [num-iterations] [inner-iter]`
    - Example: `harness Json 10 1000`