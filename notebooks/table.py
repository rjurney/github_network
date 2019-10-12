from prettytable import PrettyTable

class Table:
  def __init__(self, columns, data, null_fix=None):
    if not len(columns):
      raise "Table must have at least one column"
    if not len(data):
      raise "Table must have at least one row"
    self._columns = columns
    self._data = data    
    self._p = PrettyTable(self._columns)
    
    for row in data:
      if len(row) < len(self._columns):
        shortfall = len(self._columns) - len(row)
        for i in range(0, shortfall):
          row = row + tuple([null_fix])
      self._p.add_row(row)
  
    self._p.padding_width = 1
    self._p.float_format='.3f'
    for key in self._p.align.keys():
      self._p.align[key] = 'l'
  
  def __str__(self):
    return str(self._p)
