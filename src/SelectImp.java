import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;

public class SelectImp {
	
	static String[] table = new String[2];
	static String[] invisible_cloum = new String[2];
	static SQLBinaryOpExpr[] invisible_row = new SQLBinaryOpExpr[2];
	static SQLCharExpr asterisk = new SQLCharExpr("*");
	
	public static void main(String[] args) {
		
		List<String> sql = new ArrayList<String>();
		/*
		 * t_id > 10
		 * 列 secret 不可见
		 * 字段：t_id, name, salary, secret
		 */
		sql.add("select name from teacher as tea where salary < 1000;");
		sql.add("select t_id, name from teacher where salary > 1000 and name = \"John\";");
		sql.add("select name, t_secret from teacher where salary <= 2000;");
		/*
		 * s_id > 10
		 * 列 secret 不可见
		 */
		sql.add("select name from student where age > 13;");
		sql.add("select t_id, name, s_secret from student where age < 20;");
		
//		sql.add("INSERT INTO Persons VALUES ('Gates', 'Bill', 'Xuanwumen 10', 'Beijing');");
//		sql.add("UPDATE Person SET FirstName = 'Fred' WHERE LastName = 'Wilson';");
//		sql.add("DELETE FROM Person WHERE LastName = 'Wilson';");
		
		table[0] = "teacher";
		table[1] = "student";
		
		invisible_cloum[0] = "t_secret";
		invisible_cloum[1] = "s_secret";
		
		SQLIntegerExpr constrain_number = new SQLIntegerExpr(10);
		SQLBinaryOperator op = SQLBinaryOperator.GreaterThan;
		SQLIdentifierExpr s_id = new SQLIdentifierExpr("s_id");
		SQLIdentifierExpr t_id = new SQLIdentifierExpr("t_id");
		
		invisible_row[0] = new SQLBinaryOpExpr(t_id, op, constrain_number);
		invisible_row[1] = new SQLBinaryOpExpr(s_id, op, constrain_number);
//		SQLBinaryOpExpr teacher_row = new SQLBinaryOpExpr(t_id, op, constrain_number);
//		SQLBinaryOpExpr student_row = new SQLBinaryOpExpr(s_id, op, constrain_number);
		
		for(int i = 0;i < sql.size();i++) {
			SQLStatementParser parse = new SQLStatementParser(sql.get(i));
			SQLStatement statement = parse.parseStatement();
			
			System.out.println("=======================");
			System.out.println(statement.toString());
			System.out.println("+++++++++++++++++++++++");
			
			if(statement instanceof SQLSelectStatement) {
				//解析select
				System.out.println("//Select开始解析");
				SQLSelectStatement selectStatement = (SQLSelectStatement) statement;
				parseSelectClause(selectStatement);
				System.out.println("//解析结束");
				System.out.println(statement.toString());
			}
			else if(statement instanceof SQLInsertStatement) {
				//TODO
				System.out.println("Insert Clause.");
			}
			else if(statement instanceof SQLUpdateStatement) {
				//TODO
				System.out.println("Update Clause.");
			}
			else if(statement instanceof SQLDeleteStatement) {
				//TODO
				System.out.println("Delete Clause.");
			}
			System.out.println("=======================");
		}
		
		return;
	}
	
	public static void parseSelectClause(SQLSelectStatement selectStatement) {
		SQLSelect select = selectStatement.getSelect();
		SQLSelectQuery selectQuery = select.getQuery();
		SQLSelectQueryBlock sqlSQB = (SQLSelectQueryBlock) selectQuery;
		
		List<SQLSelectItem> selectItem = sqlSQB.getSelectList();
		SQLTableSource tableSource = sqlSQB.getFrom();
		SQLExpr where = sqlSQB.getWhere();
		
		if(tableSource instanceof SQLExprTableSource) {
			SQLExprTableSource realTableSource = (SQLExprTableSource) tableSource;
			System.out.println("//" + realTableSource.getExpr().toString());
			
			System.out.println("//Alias: " + realTableSource.getAlias());
			String tableName = realTableSource.getExpr().toString();
			int tableIndex = 0;
			for(int i = 0;i < table.length;i++) {
				if(table[i].equals(tableName)) {
					tableIndex = i;
					break;
				}
			}
			System.out.println("//tableIndex: " + tableIndex);
			//这里简化了，其实应该根据表名查到对应的所有行的过滤规则，通过addWhere方法进行修改
			sqlSQB.addWhere(invisible_row[tableIndex]);
			
//			String replace_alias = "Nice";
//			SQLSelectItem replace = new SQLSelectItem(selectItem.get(0).getExpr(), replace_alias);
//			selectItem.set(0, replace);
			for(int i = 0;i < selectItem.size();i++) {
				if(selectItem.get(i).getExpr() instanceof SQLIdentifierExpr) {
					SQLExpr tmpExpr = selectItem.get(i).getExpr();
					SQLIdentifierExpr tmpIdent = (SQLIdentifierExpr) tmpExpr;
					String attr_name = tmpIdent.getSimpleName();
					System.out.println("//attr_name: " + attr_name);
					
					if(attr_name.equals(invisible_cloum[tableIndex])) {
						System.out.println("//this is not visible.");
						SQLSelectItem encry = new SQLSelectItem(asterisk,attr_name);
						selectItem.set(i, encry);
					}
				}
				//SQLSelectItem 其他子节点类型，待完善
			}
		}
		else if(tableSource instanceof SQLJoinTableSource) {
			
		}
		else if(tableSource instanceof SQLSubqueryTableSource) {
			
		}
		
		
	}

}
