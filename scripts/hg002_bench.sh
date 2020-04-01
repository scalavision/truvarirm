#!/usr/bin/env bash

# set -x
# the test for INS will fail and exit the script, need to find a better way of handling this
# set -euo pipefail

parseArgs () {
  local OPTIND opt

  if [ "$#" == 0 ]; then
    help; exit 0
  fi

  while getopts g:d:s:f: opt; do
    case "$opt" in
      g) GOLD="$OPTARG";;
      d) SAMPLE_DIR="$OPTARG";;
      s) SAMPLE_ID="$OPTARG";;
      f) REF="$OPTARG";;
      ?) help;exit 0;;
      *) echo "unknown argument option";;
    esac
  done
  shift $((OPTIND - 1))

  if [ -z "$GOLD" ]; then
    echo "Path to gold standard folder missing: -g <your path>"
  fi 
  if [ -z "$REF" ]; then
    echo "Path to reference fasta file missing: -f <path to fasta file>"
  fi
  if [ -z "$SAMPLE_DIR" ]; then
    echo "Path to samples dir missing: -d <path to sample dir>"
  fi
  if [ -z "$SAMPLE_ID" ]; then
    echo "Path to samples dir missing: -s <path to sample dir>"
  fi

}

help() {
  echo "sv_bench.sh -g <path to gold standard dir> -d <path to sample dir> -s <path to standardized vcfs> -f <path to ref fasta file>"
}

splitOnSvType() {

  local BASE=$1
  local SVTYPE=$2

  grep '#' ./"$BASE".vcf > "$BASE"_"$SVTYPE".vcf
  grep -v '#' ./"$BASE".vcf | grep SVTYPE="$SVTYPE" > "$BASE"_TMP_"$SVTYPE".vcf

  if [ -s "$BASE"_TMP_"$SVTYPE".vcf ]; then
    cat "$BASE"_TMP_"$SVTYPE".vcf >> "$BASE"_"$SVTYPE".vcf
    bgzip "$BASE"_"$SVTYPE".vcf
    tabix -p vcf "$BASE"_"$SVTYPE".vcf.gz
    rm -rf ./"$BASE"_"$SVTYPE"_RESULT 
    truvari --prog -b "$GOLD/HG002_${SVTYPE}_Tier1_v0.6.vcf.gz" \
      -c ./"${BASE}"_"$SVTYPE".vcf.gz \
      -o ./"$BASE"_"$SVTYPE"_RESULT \
      -f "$REF" \
      --passonly \
      --pctsim=0 \
      --includebed "$GOLD_BED" \
      -r 2000 \
      --giabreport
    echo "FINISHED: $BASE with $SVTYPE"
  else
    echo "skipping file, no $SVTYPE"
  fi
#  rm ./*_TMP_"$SVTYPE".vcf
}

runTruvari() {

  echo "GOLD_DIR: $GOLD"
  echo "SAMPLE_DIR: $SAMPLE_DIR"
  echo "REF: $REF"
  echo "SAMPLE_ID: $SAMPLE_ID"

  rm -rf ./result
  mkdir ./result

  for f in "$SAMPLE_DIR"/*"${SAMPLE_ID}"*std.vcf; do
    FILE=${f##*/}
    BASE=${FILE%%_std*.*}
    rm -rf "$BASE"
    echo "$BASE"
    mkdir -p "$BASE"
    pushd "$BASE" || false
    cp "$f" ./"$BASE".vcf
    splitOnSvType "$BASE" DEL
    splitOnSvType "$BASE" INS
    mv ./"$BASE"_DEL_RESULT ../result/
    mv ./"$BASE"_INS_RESULT ../result/
    popd || false
  done

}

parseArgs "$@"
GOLD_BED="$GOLD"/HG002_SVs_Tier1_v0.6.bed
runTruvari
