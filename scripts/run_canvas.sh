#!/usr/bin/env bash

set -euo pipefail

./run.sh Diag-ValidationWGS-HG002C2a-PM /stash/workspace/results/Dragen/vcfs
./run.sh Diag-ValidationWGS2-HG002C2-PM /stash/workspace/results/Dragen/vcfs
./run.sh Diag-ValidationWGS3-HG002C2-PM /stash/workspace/results/Dragen/vcfs

