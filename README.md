# Structural Variant Report Manager

Consists of two tools:

* Truvari Report Manager
* Truvari Pipeline tool

## Truvari Report Manager

Used to autoinsert `giab_report.txt` results into Truvari Spreadsheet template.

* NB! You need to have java 8 installed to be able to run it. (java versions above 8 might also work, it's not been tested though)

This repository uses ``git lfs``. After ``git clone``, you  should run ``git lfs install``, then ``git pull``.

### Installation procedure

#### Download the prebuilt tool (binary):

[bin/truvarirm](bin/truvarirm)

```bash
chmod +x truvarirm
```

#### build from source

First you need to install `sbt`. 

* https://www.scala-sbt.org/download.html

Then issue the following commands:

```bash
git clone https://github.com/scalavision/truvarirm
cd truvarirm
# Will take some time first time you run this command
# because it downloads all dependencies
sbt assembly
```
To make a selfrunnable binary:

```bash
cd build
./make_runnable_jar.sh
cd ../bin
./truvarirm
```

## Truvari Report Manager command example

```bash
truvarirm \
  --soffice </absolute/path/to/open office executable> \
  --truvariTemplate /path/to/Truvari Report Template.ods \
  --out /absolute/path/to/output/fileName.ods \
  --report /absolute/path/to/truvari/result_folder_1:NameOfSheet1 \
            /absolute/path/to/truvari/result_folder_2:NameOfSheet2 \
            /absolute/path/to/truvari/result_folder_3:NameOfSheet3
```

The Open Office executable is typically named something like `soffice`.

The `NameOfSheet` must be unique for each truvari result.

The `--report` parameter contains `path` and ``sheet name``, separated by colon :

- path: path to the folder where the giab_report.txt file is found
- sheet name: Unique name of the Sheet for this truvari result (use simple names without funny characters, 
  there are no input validation other than whats supported by OpenOffice / LibreOffice / Excel)

Look at [run.sh](run.sh) to see an example on how to use it.

## Truvari Pipeline tool

A rather specialised script that runs truvari idempotantly on vcfs. This is not portable, as there are hardcoded paths and dependencies on not public available tools in there. Probably not valuable for others. 

* [scripts/standardize.sh](scripts/standardize.sh)

### Rationale

Runs truvari on vcf samples

Merges multiple reports based on the `giab_report.txt` truvari output into one Spreadsheet document.

Each sheet gets a name according to specification.

* Simplifies comparative analysis of different callers
* Streamlining and automative simplifications
