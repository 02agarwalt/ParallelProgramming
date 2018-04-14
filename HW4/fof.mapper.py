#!/usr/bin/env python

import sys

if __name__ == '__main__':
    val = 1
    for input_line in sys.stdin:
        input = input_line.strip().split()
        id = input[0]
        for k in range(1, len(input)):
            for m in range(k + 1, len(input)):
                friends = [id, input[k], input[m]]
                friends.sort()
                print friends[0] + " " + friends[1] + " " + friends[2] + "\t" + str(val)
        
