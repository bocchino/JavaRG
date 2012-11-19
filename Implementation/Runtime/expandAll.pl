#!/usr/bin/perl

# Expand all template files

my @targets = ( 
    ["int", "Int"], 
    ["char", "Char"], 
    ["byte", "Byte"], 
    ["boolean", "Boolean"],
    ["double", "Double"]
);

my $dir = "jrg";

foreach (`ls $dir`) {
    chomp;
    if (/(.*)\.tpt$/) {
	my $prefix = "$dir/".$1;
	foreach(@targets) {
	    my $typeName = $$_[0];
	    my $className = $$_[1];
	    my $inFileName = $prefix.".tpt";
	    my $outFileName = $prefix.$className.".java";
	    print `perl expand.pl $typeName $className < $inFileName > $outFileName\n`;
	}
    }
}
