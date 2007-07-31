package talend::filesOp;

use Exporter;
use Carp;
use List::Util qw/min/;
use File::Spec;
use File::Basename;

use vars qw(@EXPORT @ISA);

@ISA = qw(Exporter);

@EXPORT = qw(
    tFileRowCount
    getFileList
    getFirstAndLastRowNumber
    getSplittedFilename
);

sub tFileRowCount {
    my (%param) = @_;

    open(FH, $param{filename})
        or croak 'Cannot open ', $param{filename}, "\n";

    local $/ = $param{rowseparator};
    1 while <FH>;
    my $RowCount = $.;
    close FH;

    return $RowCount;
}

sub getFileList {
    # available parameters :
    #  - directory
    #  - filemask
    #  - case_sensitive
    #  - include_subdir
    my %param = @_;

    my @files;

    if ($param{include_subdir}) {
        use File::Find;
        find(
            sub{
                if ($File::Find::name ne $param{directory}) {
                    push @files, $File::Find::name;
                }
            },
            $param{directory}
        );
    }
    else {
        opendir(DIR, $param{directory});

        @files = map {
            File::Spec->catfile($param{directory}, $_);
        } readdir(DIR);

        closedir(DIR);
    }

    my @filtered_files = ();
    foreach my $file (@files) {
        my $filename = basename($file);

        if ($param{case_sensitive}) {
            if ($filename =~ m/$param{filemask}/) {
                push @filtered_files, $file;
            }
        }
        else {
            if ($filename =~ m/$param{filemask}/i) {
                push @filtered_files, $file;
            }
        }
    }

    return @filtered_files;
}

sub getFirstAndLastRowNumber {
    # header, footer, limit, total
    my %params = @_;

    my ($first, $last) = ();

    $first = 1 + $params{header};

    if (defined $params{limit}) {
        $last =
            $params{header}
            + min(
                $params{limit},
                $params{total} - $params{header} - $params{footer}
            );
    }
    else {
        $last = $params{total} - $params{footer};
    }

    return ($first, $last);
}

sub getSplittedFilename {
    my ($filename, $num) = @_;

    if ($filename =~ m{^(.*)\.([a-zA-Z0-9_-]+)$}) {
        my ($filepath_wo_ext, $ext) = ($1, $2);

        return sprintf(
            '%s-%03u.%s',
            $filepath_wo_ext,
            $num,
            $ext,
        );
    }
    else {
        return sprintf(
            '%s-%03u',
            $filename,
            $num,
        );
    }
}

1;
