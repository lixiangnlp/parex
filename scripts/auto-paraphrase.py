#!/usr/bin/env python

import gzip, os, subprocess, sys, threading

SCRIPT_DIR = os.path.dirname(__file__)

def main(argv):
    
    if len(argv[1:]) != 4:
        print >> sys.stderr, 'Warning: can use unbounded memory depending on data set.'
        print >> sys.stderr, 'JVM set to use 32G, recommend running with ulimit.'
        print >> sys.stderr
        print >> sys.stderr, 'Usage: {0} corpus.src corpus.tgt pt.gz out'.format(argv[0])
        sys.exit(1)
    
    out = argv[4]

    # Lowercase
    print >> sys.stderr, 'lc source'
    lowercase(argv[1], out + '.parex.src.lc')
    print >> sys.stderr, 'lc target'
    lowercase(argv[2], out + '.parex.tgt.lc')
    print >> sys.stderr, 'lc phrase table'
    lowercase(argv[3], out + '.parex.pt.gz', gz=True)
    
    # Paraphrase
    parex = ['java', '-Xmx32G', '-jar', os.path.join(SCRIPT_DIR, os.path.pardir,
      'parex-1.0.jar'), out + '.parex.src.lc', out + '.parex.tgt.lc',
      out + '.parex.pt.gz', out + '.parex.src.lc', out + '.parex.tgt.lc',
      out + '.parex']
    subprocess.call(parex)
    
    # Filter source
    vacuum = ['java', '-Xmx32G', '-cp', os.path.join(SCRIPT_DIR, os.path.pardir,
      'parex-1.0.jar'), 'Vacuum', '0.01', out + '.parex.f.par.gz',
      out + '.parex.vac.src.gz']
    subprocess.call(vacuum)
    
    # Filter target
    vacuum = ['java', '-Xmx32G', '-cp', os.path.join(SCRIPT_DIR, os.path.pardir,
      'parex-1.0.jar'), 'Vacuum', '0.01', out + '.parex.n.par.gz',
      out + '.parex.vac.tgt.gz']
    subprocess.call(vacuum)
    
    # Convert format
    print >> sys.stderr, 'format source'
    mformat(out + '.parex.vac.src.gz', out + '.parex.m.src.gz')
    print >> sys.stderr, 'format target'
    mformat(out + '.parex.vac.tgt.gz', out + '.parex.m.tgt.gz')
    
    # Remove word accumulation source
    print >> sys.stderr, 'rm words source'
    rmacc = [os.path.join(SCRIPT_DIR, 'rm_word_accumulation.py'),
      out + '.parex.m.src.gz', out + '.paraphrase.src.gz']
    subprocess.call(rmacc)
    
    # Remove word accumulation target
    print >> sys.stderr, 'rm words target'
    rmacc = [os.path.join(SCRIPT_DIR, 'rm_word_accumulation.py'),
      out + '.parex.m.tgt.gz', out + '.paraphrase.tgt.gz']
    subprocess.call(rmacc)
    
    print >> sys.stderr, ''
    print >> sys.stderr, 'Source and target paraphrase tables written to:'
    print >> sys.stderr, out + '.paraphrase.src.gz'
    print >> sys.stderr, out + '.paraphrase.tgt.gz'
    print >> sys.stderr, ''
    print >> sys.stderr, 'You can safely delete intermediate files with:'
    print >> sys.stderr, 'rm ' + out + '.parex.*'

def lowercase(f, out, gz=False):
    f_in = gzip.open(f) if is_gz(f) else open(f)
    f_out = gzip.open(out, 'wb') if gz else open(out, 'w')
    lc = ['perl', os.path.join(SCRIPT_DIR, 'lowercase.perl')]
    p_lc = subprocess.Popen(lc, stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    t_cat = threading.Thread(target=cat, args=(p_lc.stdout, f_out))
    t_cat.start()
    for line in f_in:
        p_lc.stdin.write(line)
    p_lc.stdin.close()
    p_lc.wait()
    t_cat.join()
    f_out.close()

# Always gzipped
def mformat(f, out):
    f_in = gzip.open(f)
    f_out = gzip.open(out, 'wb')
    fmt = [os.path.join(SCRIPT_DIR, 'meteor_fmt.sh')]
    p_fmt = subprocess.Popen(fmt, stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    t_cat = threading.Thread(target=cat, args=(p_fmt.stdout, f_out))
    t_cat.start()
    for line in f_in:
        p_fmt.stdin.write(line)
    p_fmt.stdin.close()
    p_fmt.wait()
    t_cat.join()
    f_out.close()

def is_gz(f):
    in_f = open(f, 'rb')
    gz = ord(in_f.read(1)) == 0x1f and ord(in_f.read(1)) == 0x8b
    in_f.close()
    return gz

def cat(in_f, out_f):
    for line in in_f:
        out_f.write(line)

if __name__ == '__main__' : main(sys.argv)
