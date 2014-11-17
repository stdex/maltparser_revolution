import os
import sys
import numpy as np
from sklearn import linear_model
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from scipy.sparse import csr_matrix
import csv

def ReadCsv(fileName, mode):
    in_file = open(fileName)
    reader = csv.reader(in_file, delimiter='\t', quotechar='"')
    if(mode == 'input'):
        data = [[]]
        for row in reader:
            data.append(row)
        data.pop(0)
    elif(mode == 'output'):
        column = 1
        data = []
        for row in reader:
            data.append(int(row[column]))

    return data

# in_filepath = sys.argv[1]
in_filepath = "/home/rostunov/workspace/neuro/maltparser/malt/data/x.txt"
X = np.asarray(ReadCsv(in_filepath, 'input'), 'int')

# A = csr_matrix(X)
# print (A)


