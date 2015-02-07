package com.sirchen.file2db;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class CodeFinder {
	// 码表表名
		private String tablename;
		// 要插入的字段，一般都为code或者id
		private String columnTo;
		// 实际字段
		private String columnFrom;
		// 具体值
		private String value;

		public String getTablename() {
			return tablename;
		}

		public void setTablename(String tablename) {
			this.tablename = tablename;
		}

		public String getColumnTo() {
			return columnTo;
		}

		public void setColumnTo(String columnTo) {
			this.columnTo = columnTo;
		}

		public String getColumnFrom() {
			return columnFrom;
		}

		public void setColumnFrom(String columnFrom) {
			this.columnFrom = columnFrom;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
}
