#!/usr/bin/env bash

set -x
set -euo pipefail

THIS_SCRIPT=${BASH_SOURCE[0]}
DIR="$( cd "$( dirname "${THIS_SCRIPT}" )" && pwd )"

"${DIR}"/hg002_bench.sh -g /stash/workspace/AshkenazimSVGold \
    -d /stash/workspace/results/vcpipe-sv-run-2020_04_01_1330 \
    -s HG002 \
    -f /stash/sources/amg/vcpipe-bundle/genomic/gatkBundle_2.5/human_g1k_v37_decoy.fasta

