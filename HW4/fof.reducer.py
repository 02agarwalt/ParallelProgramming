#!/usr/bin/env python

import sys

if __name__ == '__main__':
    curr = None
    counter = 1
    for input_line in sys.stdin:
        k, v = input_line.split("\t", 1)
        if curr == k:
            counter += int(v)
            if counter == 3:
                k = k.split()
                temp1 = min(k[1], k[2])
                temp2 = max(k[1], k[2])
                temp3 = min(k[0], k[2])
                temp4 = max(k[0], k[2])
                temp5 = min(k[1], k[0])
                temp6 = max(k[1], k[0])
                print k[0] + " " + temp1 + " " + temp2
                print k[1] + " " + temp3 + " " + temp4
                print k[2] + " " + temp5 + " " + temp6
        else:
            curr = k    
