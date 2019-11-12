#!/usr/bin/env bash

set -euo pipefail

#In order to use this script, you have to set the following variables
#
#* BASE: Base directory for running all the jobs
#* RESULT_PATH: Path to the vcf results
#* RESULTS_DEL: The vcf files in RESULT_PATH that you want to process for DEL
#* RESULTS_INS: The vcf files in RESULT_PATH that you want to process for INS
#* RESULTS: The union of RESULTS_DEL and RESULTS_INS (have to be specified manually as well, due to limiations in bash)
#* TRUVARI_NEW: The path to truvari
#* REF_FOLDER: folder to human decoy fasta ref file.
#* HG002_GOLD: Path to Tier1 vcf.gz files

# svmerger is run inside singularity, you may want to change the SV_MERGER_TRUVARI variable as well.

# The base directory for all folders
BASE="/workspace/results/truvari_1_4"

# The path to the raw vcf output from the different callers
RESULT_PATH="$BASE/tir_22_okt"
#TRUVARI_NEW=/nix/store/1xl9zkms1nlcyyn9pj5wicidck9lkrck-truvari-1.3.2/bin/truvari
#TRUVARI_NEW=/nix/store/vcly9bcfp8h2wznkb2ifq082fxygxdk5-truvari-1.3.2/bin/truvari
TRUVARI_NEW=/nix/store/vlanmbkv2clrcbryb3b8lpmgcli0qiph-truvari-1.3.4/bin/truvari
# TRUVARI_NEW=/nix/store/32a8k91fsmzpgqp8z94n3vwxb2w085jr-python3.5-truvari-1.2/bin/truvari

# The resulting vcfs files inside the RESULT_PATH
RESULTS_DEL="manta_NACG-HG002-BenchM_diploidSV.vcf cnvnator_NACG-HG002-BenchM.vcf delly_NACG-HG002-BenchM.vcf tiddit_NACG-HG002-BenchM.vcf"
RESULTS_INS="manta_NACG-HG002-BenchM_diploidSV.vcf delly_NACG-HG002-BenchM.vcf" 
RESULTS="manta_NACG-HG002-BenchM_diploidSV.vcf cnvnator_NACG-HG002-BenchM.vcf delly_NACG-HG002-BenchM.vcf tiddit_NACG-HG002-BenchM.vcf"

# File Paths that contains the gold standard test files.
HG002_GOLD="/workspace/AshkenazimSVGold"
HG002_VCF="HG002_SVs_Tier1_v0.6.vcf.gz"
HG002_BED="HG002_SVs_Tier1_v0.6.bed"
HG002_DEL="$HG002_GOLD/HG002_DELs_Tier1_v0.6.vcf.gz"
HG002_INS="$HG002_GOLD/HG002_INS_Tier1_v0.6.vcf.gz"



# Paths used for standardizing the vcfs to make them available for truvari The excellent svmerger tool by sjurug is used 
NORMALIZED_TRUVARI="$BASE/hg002_truvari" 
NORMALIZED_DEL_PATH="$BASE/hg002_standardized_DEL" 
NORMALIZED_INS_PATH="$BASE/hg002_standardized_INS"

NORMALIZED_SVTK="$BASE/hg002_svtk"

# Merged paths, merging is not working that well atm.
MERGED_SVTK="$BASE/svtk_merged"

# Output paths for vcfs that have been standardized, zipped and indexed
INDEXED_PATH="$BASE/hg002_standardized_and_indexed"
INDEXED_DEL_PATH="$BASE/hg002_standardized_and_indexed_with_DEL"
INDEXED_INS_PATH="$BASE/hg002_standardized_and_indexed_with_INS"

# Result path after TRUVARI has finished running
TRUVARI_BASE="$BASE/truvari_results"
TRUVARI_BASE_DEL="$BASE/truvari_results_DEL"
TRUVARI_BASE_INS="$BASE/truvari_results_INS"


# Path to singularity containing the svmerger scripts
#SINGULARITY="/nix/store/lavaszlrqjll0rhsb65s3axambpzpnay-singularity-2.6.0/bin/singularity exec -H /home/obiwan/tmp"
SINGULARITY="sudo singularity exec"
#SVMERGER="/home/obiwan/Projects/sv_merge/svmerger"
SVMERGER="/stash/usit/cluster/projects/p22/dev/p22-tomegil/imgs/svm.sif"

# svmerger commands
SV_MERGER_TRUVARI="$SINGULARITY -B $RESULT_PATH:/in -B $NORMALIZED_TRUVARI:/out $SVMERGER python -m svmerger"
SV_MERGER_SVTK="$SINGULARITY -B $RESULT_PATH:/in -B $NORMALIZED_SVTK:/out $SVMERGER python -m svmerger"
SV_MERGER_MERGE="$SINGULARITY -B $NORMALIZED_SVTK:/in -B $MERGED_SVTK:/out $SVMERGER svtk"

# Path to references
REF_PATH="/stash/sources/amg/vcpipe-bundle/genomic/gatkBundle_2.5"
# TRUVARI="$SINGULARITY -B $INDEXED_PATH:/in -B $TRUVARI_BASE:/out -B $REF_PATH:/ref -B $HG002_GOLD:/vcf $SVMERGER truvari"
REF_FILE="human_g1k_v37_decoy.fasta"

function normalizeVcfsForTruvari() {
  rm -rf $NORMALIZED_TRUVARI || true
  mkdir -p $NORMALIZED_TRUVARI

  for FILE in $RESULTS; do
    $SV_MERGER_TRUVARI standardize --filenames "/in/$FILE" --outfile "/out/$FILE" --truvari
  done

}

function normalizeVcfsForSvtk() {

  rm -rf $NORMALIZED_SVTK || true
  mkdir -p $NORMALIZED_SVTK

  for FILE in $RESULTS; do
    $SV_MERGER_SVTK standardize --filenames "/in/$FILE" --outfile "/out/$FILE" --svtk
    echo "/in/$FILE" >> "$NORMALIZED_SVTK/svtk_filelist.txt"
  done

}

function mergeVCFs() {
  rm -rf $MERGED_SVTK || true
  rm -rf $TRUVARI_BASE/merged_svtk || true
  mkdir -p $MERGED_SVTK
  $SV_MERGER_MERGE vcfcluster "/in/svtk_filelist.txt" "/out/svtk.vcf" -d 500 -f 0.5 -z 0 -t DEL,DUP,INV,BND,INS --preserve-ids
  bcftools sort "$MERGED_SVTK/svtk.vcf" > "$MERGED_SVTK/sorted_svtk.vcf"
  bgzip "$MERGED_SVTK/sorted_svtk.vcf"
  tabix -p vcf "$MERGED_SVTK/sorted_svtk.vcf.gz"
  truvari -b "$HG002_GOLD/$HG002_VCF" -c "$MERGED_SVTK/sorted_svtk.vcf.gz" -o "$TRUVARI_BASE/merged_svtk" -f "$REF_PATH/$REF_FILE" --passonly --includebed "$HG002_GOLD/$HG002_BED" -r 2000 --giabreport
}

# Unused for now
function normalizeSvType() {

  FILE=$1
  SVPATH=$2
  OUTPATH=$3

  $SINGULARITY -B $SVPATH:/in -B $OUTPATH:/out $SVMERGER python -m svmerger standardize --filenames /in/$FILE --outfile /out/$FILE --svtk
  cp $OUTPATH/$FILE $SVPATH/$FILE

}

function extractSvType() {

  SVTYPE_PATH=$1
  SVTYPE=$2
  FILES=$3

  echo "EXTRACTING FILES: $FILES"

  if [[ $SVTYPE_PATH == *"DEL"* ]]; then
    OUTPATH=$NORMALIZED_DEL_PATH
    mkdir -p $OUTPATH

    for FILE in $RESULTS_DEL; do
      echo "extracting $SVTYPE from $FILE"
      grep "^#" $NORMALIZED_TRUVARI/$FILE > "$SVTYPE_PATH/${FILE}_header"
      grep SVTYPE=$SVTYPE $NORMALIZED_TRUVARI/$FILE > "$SVTYPE_PATH/${FILE}_body"
      cat "$SVTYPE_PATH/${FILE}_body" >> "$SVTYPE_PATH/${FILE}_header"
      mv "$SVTYPE_PATH/${FILE}_header" "$SVTYPE_PATH/$FILE"
      rm "$SVTYPE_PATH/${FILE}_body"
      $SINGULARITY -B $SVTYPE_PATH:/in -B $OUTPATH:/out $SVMERGER python -m svmerger standardize --filenames /in/$FILE --outfile /out/$FILE --svtk
      cp $OUTPATH/$FILE $SVTYPE_PATH/$FILE
      bgzip "$SVTYPE_PATH/$FILE"
      tabix -p vcf "$SVTYPE_PATH/${FILE}.gz"
     done
  elif [[ $SVTYPE_PATH == *"INS"* ]]; then
    OUTPATH=$NORMALIZED_INS_PATH
    mkdir -p $OUTPATH
    for FILE in $RESULTS_INS; do
      echo "extracting $SVTYPE from $FILE"
      grep "^#" $NORMALIZED_TRUVARI/$FILE > "$SVTYPE_PATH/${FILE}_header"
      grep SVTYPE=$SVTYPE $NORMALIZED_TRUVARI/$FILE > "$SVTYPE_PATH/${FILE}_body"
      cat "$SVTYPE_PATH/${FILE}_body" >> "$SVTYPE_PATH/${FILE}_header"
      mv "$SVTYPE_PATH/${FILE}_header" "$SVTYPE_PATH/$FILE"
      rm "$SVTYPE_PATH/${FILE}_body"
      $SINGULARITY -B $SVTYPE_PATH:/in -B $OUTPATH:/out $SVMERGER python -m svmerger standardize --filenames /in/$FILE --outfile /out/$FILE --svtk
      cp $OUTPATH/$FILE $SVTYPE_PATH/$FILE
      bgzip "$SVTYPE_PATH/$FILE"
      tabix -p vcf "$SVTYPE_PATH/${FILE}.gz"
    done
  else
    echo "Not able to determine output path"
    exit 1;
  fi


}

function extractINSs() {
  rm -rf $INDEXED_INS_PATH 
  mkdir -p $INDEXED_INS_PATH 
  extractSvType $INDEXED_INS_PATH "INS" $RESULTS_INS
}

function extractDELs() {
  rm -rf $INDEXED_DEL_PATH 
  mkdir -p $INDEXED_DEL_PATH 
  extractSvType $INDEXED_DEL_PATH "DEL" $RESULTS_DEL
}

function indexVcfs() {
  
  rm -rf $INDEXED_PATH
  mkdir -p $INDEXED_PATH

  for FILE in $RESULTS; do
    cp $NORMALIZED_TRUVARI/$FILE $INDEXED_PATH/$FILE
    bgzip $INDEXED_PATH/$FILE
    tabix -p vcf "$INDEXED_PATH/$FILE.gz"
  done

}

#TRUVARI_NEW=/nix/store/rnbkdgdg9q6hzgacij304h2zsjbmm1dx-python3.7-truvari-1.3.2/bin/truvari

function compareTruvari() {

  DIR=$1
  BASE_VCF=$2

  rm -rf $DIR || true
  mkdir -p $DIR

  for FILE in $RESULTS_DEL; do
    # --pctsim=0 
    $TRUVARI_NEW -b "$BASE_VCF" -c "$INDEXED_DEL_PATH/$FILE.gz" -o "$DIR/${FILE%.vcf}" -f "$REF_PATH/$REF_FILE" --passonly --includebed "$HG002_GOLD/$HG002_BED" -r 2000 --giabreport
    # Running via singularity
    # $TRUVARI -b "/vcf/$HG002_VCF" -c "/in/$FILE.gz"  -o "/out/${FILE%.vcf}" -f "/ref/$REF_FILE" --passonly --includebed "/vcf/$HG002_BED" -r 2000 --giabreport
#    echo truvari -b "$BASE_VCF" -c "$INDEXED_DEL_PATH/$FILE.gz" -o "$DIR/${FILE%.vcf}" -f "$REF_PATH/$REF_FILE" --passonly --includebed "$HG002_GOLD/$HG002_BED" -r 2000 --giabreport
  done

}


# TODO: add these options for INS
# --pctsim=0 --pctovl=0.0 --pctsize=0.0
function compareTruvariINS() {

  echo "Comapring truvari insertions"
  DIR=$TRUVARI_BASE_INS
  BASE_VCF=$HG002_INS

  rm -rf $DIR || true
  mkdir -p $DIR

  for FILE in $RESULTS_INS; do
    # pctsim should not be needed, have to test this ...
    $TRUVARI_NEW -b "$BASE_VCF" -c "$INDEXED_INS_PATH/$FILE.gz" -o "$DIR/${FILE%.vcf}" -f "$REF_PATH/$REF_FILE" --passonly --pctsim=0 --includebed "$HG002_GOLD/$HG002_BED" -r 2000 --giabreport
#    TRUVARI_NEW -b "$BASE_VCF" -c "$INDEXED_INS_PATH/$FILE.gz" -o "$DIR/${FILE%.vcf}" -f "$REF_PATH/$REF_FILE" --passonly --includebed "$HG002_GOLD/$HG002_BED" -r 2000 --giabreport
  done
#  compareTruvari $TRUVARI_BASE_INS $HG002_INS
}

function compareTruvariDEL() {
  echo "Comparing truvari deletions"
  compareTruvari $TRUVARI_BASE_DEL $HG002_DEL
}

function compareTruvariAll() {
  compareTruvari $TRUVARI_BASE "$HG002_GOLD/$HG002_VCF"
}

echo "Normalizing for Truvari"
#normalizeVcfsForTruvari
echo "Extracting deletions"
#extractDELs
echo "Extracting insertions"
#extractINSs
echo "comparing DEL"
#compareTruvariDEL
echo "Comparing INS"
compareTruvariINS

