/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.knox.gateway.shell.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.knox.gateway.util.JsonUtils;


/**
 * Simple table representation and text based rendering of a table via
 * toString(). Headers are optional but when used must have the same count as
 * columns within the rows.
 */
public class KnoxShellTable {

  List<String> headers = new ArrayList<String>();
  List<List<Comparable<? extends Object>>> rows = new ArrayList<List<Comparable<? extends Object>>>();
  String title;

  public KnoxShellTable title(String title) {
    this.title = title;
    return this;
  }

  public KnoxShellTable header(String header) {
    headers.add(header);
    return this;
  }

  public KnoxShellTable row() {
    rows.add(new ArrayList<Comparable<? extends Object>>());
    return this;
  }

  public KnoxShellTable value(Comparable<? extends Object> value) {
    final int index = rows.isEmpty() ? 0 : rows.size() - 1;
    final List<Comparable<? extends Object>> row = rows.get(index);
    row.add(value);
    return this;
  }

  public KnoxShellTableCell<? extends Comparable<? extends Object>> cell(int colIndex, int rowIndex) {
    return new KnoxShellTableCell(headers, rows, colIndex, rowIndex);
  }

  public List<Comparable<? extends Object>> values(int colIndex) {
    List<Comparable<? extends Object>> col = new ArrayList<Comparable<? extends Object>>();
    rows.forEach(row -> col.add(row.get(colIndex)));
    return col;
  }

  public List<Comparable<? extends Object>> values(String colName) {
    int colIndex = headers.indexOf(colName);
    List<Comparable<? extends Object>> col = new ArrayList<Comparable<? extends Object>>();
    rows.forEach(row -> col.add(row.get(colIndex)));
    return col;
  }

  public KnoxShellTable apply(KnoxShellTableCell<? extends Comparable<? extends Object>> cell) {
    if (!headers.isEmpty()) {
      headers.set(cell.colIndex, cell.header);
    }
    if (!rows.isEmpty()) {
      rows.get(cell.rowIndex).set(cell.colIndex, cell.value);
    }
    return this;
  }

  public List<String> getHeaders() {
    return headers == null || headers.isEmpty() ? null : headers;
  }

  public List<List<Comparable<? extends Object>>> getRows() {
    return rows;
  }

  public String getTitle() {
    return title;
  }

  public static KnoxShellTableBuilder builder() {
    return new KnoxShellTableBuilder();
  }

  public KnoxShellTableFilter filter() {
    return new KnoxShellTableFilter().table(this);
  }

  public KnoxShellTable select(String cols) {
    KnoxShellTable table = new KnoxShellTable();
    List<List<Comparable<? extends Object>>> columns = new ArrayList<List<Comparable<? extends Object>>>();
    String[] colnames = cols.split(",");
    for (String colName : colnames) {
      table.header(colName);
      columns.add(values(headers.indexOf(colName)));
    }
    for (int i = 0; i < rows.size(); i++) {
      table.row();
      for (List<Comparable<? extends Object>> col : columns) {
        table.value(col.get(i));
      }
    }
    return table;
  }

  public KnoxShellTable sort(String colName) {
    KnoxShellTable table = new KnoxShellTable();

    Comparable<? extends Object> value;
    List<Comparable<? extends Object>> col = values(colName);
    List<RowIndex> index = new ArrayList<RowIndex>();
    for (int i = 0; i < col.size(); i++) {
      value = col.get(i);
      index.add(new RowIndex(value, i));
    }
    Collections.sort(index);
    table.headers = new ArrayList<String>(headers);
    for (RowIndex i : index) {
      table.rows.add(new ArrayList<Comparable<? extends Object>>(this.rows.get(i.index)));
    }
    return table;
  }

  private static class RowIndex implements Comparable<RowIndex> {
    Comparable value;
    int index;

    RowIndex(Comparable<? extends Object> value, int index) {
      this.value = value;
      this.index = index;
    }

    @Override
    public int compareTo(RowIndex other) {
      return this.value.compareTo(other.value);
    }
  }

  @Override
  public String toString() {
    return new KnoxShellTableRenderer(this).toString();
  }

  public String toJSON() {
    return JsonUtils.renderAsJsonString(this);
  }

  public String toCSV() {
    return new KnoxShellTableRenderer(this).toCSV();
  }

}