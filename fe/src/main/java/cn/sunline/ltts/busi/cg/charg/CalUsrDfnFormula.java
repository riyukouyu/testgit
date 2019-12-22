package cn.sunline.ltts.busi.cg.charg;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalUsrDfnFormula {

    class OPNode
    {
        char op;// 运算符号
        int level;// 优先级

        public OPNode(String op)
        {
            this.op = op.charAt(0);
            if (op.equals("+") || op.equals("-"))
            {
                this.level = 1;
            }
            else if (op.equals("*") || op.equals("/"))
            {
                this.level = 2;
            }
            else if (op.equals("("))
            {
                this.level = -3;
            }
            else
            {
                this.level = -1;
            }
        }
    }

    public Object calc(String expression)
    {
        List<Object> list = new LinkedList<Object>();
        // 正则式
        Pattern entryOfExpression = Pattern.compile("[0-9]+(\\.[0-9]+)?|\\(|\\)|\\+|-|\\*|/");
        Deque<Object> stack = new LinkedList<Object>();// 栈
        Matcher m = entryOfExpression.matcher(expression);
        while (m.find())
        {
            // 提取语素
            String nodeString = expression.substring(m.start(), m.end());
            if (nodeString.matches("[0-9].*"))
            {
                list.add(Double.valueOf(nodeString));// 如果是数字直接送入列表
            }
            else
            {
                OPNode opn = new OPNode(nodeString);// 如果是运算符
                int peekLevel = (stack.peek() == null) ? 0 : ((OPNode) stack.peek()).level;
                if (opn.level >= peekLevel)
                {
                    stack.push(opn);// 新的运算符比旧的优先级别高则入栈
                }
                else
                {
                    if (opn.level == -1)
                    {
                        OPNode temp = (OPNode) stack.pop();
                        while (temp.level != -3)
                        {// 如果为"("则一直出栈一直到")"
                            list.add(temp);
                            temp = (OPNode) stack.pop();
                        }
                    }
                    else if (opn.level == -3)
                    {
                        stack.push(opn);
                    }
                    else
                    {// 如果新运算符比栈顶运算符底则一直出栈
                        OPNode temp = (OPNode) stack.pop();
                        while (temp.level > opn.level)
                        {
                            list.add(temp);
                            if (stack.isEmpty())
                                break;
                            temp = (OPNode) stack.pop();
                        }
                        stack.push(opn);
                    }
                }
            }

        }
        OPNode temp = null;
        while (!stack.isEmpty())
        {
            temp = (OPNode) stack.pop();
            list.add(temp);
        }// 后续表达式计算
        stack.clear();

        for (Object o : list)
        {
            if (o instanceof Double)
            {
                stack.push(o);// 为数字入栈
            }
            else {
                double op2 = ((Double) stack.pop()).doubleValue();

                double op1 = ((Double) stack.pop()).doubleValue();

                switch (((OPNode) o).op) {
                case '+':
                    stack.push(op1 + op2);
                    break;
                case '-':
                    stack.push(op1 - op2);
                    break;

                case '*':
                    stack.push(op1 * op2);
                    break;

                case '/':
                    stack.push(op1 / op2);
                    break;
                }

            }
        }
        return stack.pop();
    }

}
