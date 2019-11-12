# Structural Variant Report Manager

Consists of two tools:

* Truvari Report Manager
* Truvari Pipeline tool

## Truvari Report Manager

To autoinsert `giab_report.txt` results into Truvari Spreadsheet template, download the tool (binary):

* [bin/truvarirm](bin/truvarirm)

```bash
chmod +x truvarirm
```

NB! You need to have java installed to be able to run it.

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

A script that runs truvari on vcfs

* [scripts/truvari.sh](scripts/truvari.sh)

## Rationale

Runs truvari on vcf samples

Merges multiple reports based on the `giab_report.txt` truvari output into one Spreadsheet document.

Each sheet gets a name according to specification.

* Simplifies comparative analysis of different callers
* Streamlining and automative simplifications
