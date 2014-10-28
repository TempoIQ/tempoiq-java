package com.tempoiq;

import java.util.Iterator;

public class DataPointCursor implements Cursor<DataPoint> {
  private class DataPointIterator implements Iterator<DataPoint> {
    private final Iterator<Row> rowIterator;
    private final String deviceKey;
    private final String sensorKey;
    private Row nextRow;
    
    public DataPointIterator(DataPointRowCursor rowCursor, String deviceKey, String sensorKey) {
      this.rowIterator = rowCursor.iterator();
      System.out.println("ROW ITERATOR: ");
      for(Row r : rowCursor) {
        System.out.println(r.toString());
      }
      this.deviceKey = deviceKey;
      this.sensorKey = sensorKey;
    }

    @Override
    public boolean hasNext() {
      while (rowIterator.hasNext()) {
	nextRow = rowIterator.next();
	if (nextRow.hasSensor(deviceKey, sensorKey)) {
	  return true;
	}
      }

      return false;
    }

    @Override
    public DataPoint next() {
      return new DataPoint(nextRow.getTimestamp(), nextRow.getValue(deviceKey, sensorKey));
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private final String deviceKey;
  private final String sensorKey;
  private final DataPointRowCursor rowCursor;

  public DataPointCursor(DataPointRowCursor rowCursor, String deviceKey, String sensorKey) {
    this.rowCursor = rowCursor;
    this.deviceKey = deviceKey;
    this.sensorKey = sensorKey;
  }

  public Iterator<DataPoint> iterator() {
    return new DataPointIterator(rowCursor, deviceKey, sensorKey);
  }
}
