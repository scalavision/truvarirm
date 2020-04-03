#!/usr/bin/env bash

set -x
set -euo pipefail

SAMPLE=$1
SAMPLE_PATH=${2:-/stash/workspace/results/"$SAMPLE"}
THIS_SCRIPT=${BASH_SOURCE[0]}
DIR="$( cd "$( dirname "${THIS_SCRIPT}" )" && pwd )"

"${DIR}"/hg002_bench.sh -g /stash/workspace/AshkenazimSVGold \
    -d "$SAMPLE_PATH" \
    -s "$SAMPLE" \
    -f /stash/sources/amg/vcpipe-bundle/genomic/gatkBundle_2.5/human_g1k_v37_decoy.fasta
