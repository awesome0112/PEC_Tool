package utils.autoUnitTestUtil.parser;

//import com.sun.istack.internal.NotNull;

import utils.autoUnitTestUtil.cfg.*;
import utils.autoUnitTestUtil.utils.Utils;
import org.eclipse.jdt.core.dom.*;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ASTHelper {
    public enum Coverage {
        STATEMENT,
        BRANCH,
        MCDC,
        PATH
    }
    protected static List<String> primitiveTypes = Arrays.asList("boolean", "short", "int", "long", "float", "double", "void");
    protected static List<String> javaLangTypes = Arrays.asList("Boolean", "Byte", "Character.Subset", "Character.UnicodeBlock", "ClassLoader", "Double",
            "Float", "Integer", "Long", "Math", "Number", "Object", "Package", "Process", "Runtime",
            "Short", "String", "StringBuffer", "StringBuilder", "System", "Thread", "ThreadGroup",
            "Throwable", "Void");

    private static Stack<CfgNode> endNodeStack = new Stack<>();// for break statements
    private static Stack<CfgNode> conditionNodeStack = new Stack<>(); // for continue statements
    private static CfgEndBlockNode endCfgNode = null; // for return statements

    private static int totalStatement;
    private static int totalBranch;

    public static String getFullyQualifiedName(Type type, CompilationUnit cu) {
        if (type.isParameterizedType()) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return getFullyQualifiedTypeName(parameterizedType, cu);
        } else if (type.isArrayType()) {
            ArrayType arrayType = (ArrayType) type;
            return getFullyQualifiedTypeName(arrayType, cu);
        } else {
            return getFullyQualifiedTypeName(type.toString(), cu);
        }
    }

    protected static String getFullyQualifiedTypeName(ParameterizedType parameterizedType, CompilationUnit cu) {
        String result = "";
        String type = parameterizedType.getType().toString();
        result += getFullyQualifiedTypeName(type, cu) + "<";

        List args = parameterizedType.typeArguments();
        if (args.size() == 1) {
            String argQualifiedName = getFullyQualifiedTypeName(args.get(0).toString(), cu);
            result += argQualifiedName;
        } else {
            for (Object arg : args) {
                String argQualifiedName = getFullyQualifiedTypeName(arg.toString(), cu);
                result += argQualifiedName + ",";
            }
            result = result.substring(0, result.length() - 1);
        }
        result += ">";
        return result;
    }

    protected static String getFullyQualifiedTypeName(ArrayType arrayType, CompilationUnit cu) {
        String result = "";
        result += getFullyQualifiedTypeName(arrayType.getElementType().toString(), cu);
        for (Object dimen : arrayType.dimensions()) {
            result += dimen.toString();
        }
        return result;
    }

    protected static String getFullyQualifiedTypeName(String typeName, CompilationUnit cu) {
        // input is null or input is already a fully qualified type
        if (typeName == null || typeName.contains(".")) {
            return typeName;
        }

        // is primitive type?
        if (primitiveTypes.contains(typeName)) {
            return typeName;
        }

        // find in import statements
        for (Object o : cu.imports()) {
            if (o instanceof ImportDeclaration) {
                ImportDeclaration id = (ImportDeclaration) o;
                String idStr = id.getName().getFullyQualifiedName();
                if (idStr.endsWith("." + typeName)) {
                    return idStr;
                }
            }
        }

        // find in java.lang package
        if (javaLangTypes.contains(typeName)) {
            return "java.lang." + typeName;
        }

        PackageDeclaration packageDeclaration = cu.getPackage();
        if (packageDeclaration == null) {
            return typeName;
        } else {
            return packageDeclaration.getName() + "." + typeName;
        }
    }

    public static void generateCFGTreeFromASTNode(ASTNode astNode, CfgNode rootCFG) {

        List<ASTNode> children = Utils.getChildren(astNode);
        for (ASTNode node : children) {
            CfgNode cfgChild = null;
            if (node instanceof IfStatement) {
                cfgChild = new CfgIfStatementBlockNode();
            } else if (node instanceof TypeDeclaration || node instanceof MethodDeclaration) {
                cfgChild = new CfgStartNode();
            } else if (node instanceof FieldDeclaration) {
                cfgChild = new CfgNode();
            } else if (node instanceof Block) {
                cfgChild = new CfgBlockNode();
            } else if (node instanceof ExpressionStatement) {
                cfgChild = new CfgBoolExprNode();
            } else if (node instanceof Expression) {
                cfgChild = new CfgExpressionNode();
            } else if (node instanceof ReturnStatement) {
                cfgChild = new CfgReturnStatementNode();
            } else if (node instanceof VariableDeclarationStatement) {
                cfgChild = new CfgNormalNode();
            }
            if (cfgChild != null) {
                cfgChild.setContent(node.toString());
                cfgChild.setStartPosition(node.getStartPosition());
                cfgChild.setEndPosition(node.getStartPosition() + node.getLength());
                cfgChild.setAst(node);

                cfgChild.setParent(rootCFG);
                rootCFG.getChildren().add(cfgChild);
                generateCFGTreeFromASTNode(node, cfgChild);
            }
        }
    }

    public static CfgNode generateCFG(CfgNode block, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        endNodeStack = new Stack<>();
        conditionNodeStack = new Stack<>();
        endCfgNode = null;
        return generateCFGFromASTBlockNode(block, compilationUnit, firstLine, coverage);
    }

    private static CfgNode generateCFGFromASTBlockNode(CfgNode block, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        CfgNode beginStatementNode = block.getBeforeStatementNode();
        CfgEndBlockNode endStatementNode = (CfgEndBlockNode) block.getAfterStatementNode();

        if (endCfgNode == null) {
            endCfgNode = endStatementNode;
        }

        CfgNode cfgRootNode = null;// = beginStatementNode;

        if (block.getAst() instanceof Block) {
            //region Block processing
            List<ASTNode> statements = ((Block) block.getAst()).statements();

            if (statements.size() > 0) {
                for (int i = 0; i < statements.size(); i++) {
                    ASTNode statement = statements.get(i);

                    CfgNode currentNode = generateCFGForOneStatement(statement, beginStatementNode, endStatementNode, compilationUnit, firstLine, coverage);

                    if (currentNode instanceof CfgBeginSwitchNode) {
                        beginStatementNode = ((CfgBeginSwitchNode) currentNode).getEndBlockNode();
                    } else if (currentNode instanceof CfgBoolExprNode) {
                        beginStatementNode = ((CfgBoolExprNode) currentNode).getEndBlockNode();
                    } else if (currentNode instanceof CfgBeginForNode) {
                        beginStatementNode = ((CfgBeginForNode) currentNode).getEndBlockNode();
                    } else if (currentNode instanceof CfgBeginDoNode) {
                        beginStatementNode = ((CfgBeginDoNode) currentNode).getEndBlockNode();
                    } else if (currentNode instanceof CfgBeginForEachNode) {
                        beginStatementNode = ((CfgBeginForEachNode) currentNode).getEndBlockNode();
                    } else if (currentNode instanceof CfgBeginBlockNode) {
                        beginStatementNode = ((CfgBeginBlockNode) currentNode).getEndBlockNode();
                    } else {
                        beginStatementNode = currentNode;
                    }

                    if (i == 0) {
                        cfgRootNode = currentNode;
                    }

                }
            } else {
                cfgRootNode = block.getAfterStatementNode();
            }

            //endregion Block processing

            return cfgRootNode;
        } else {
            ASTNode statement = block.getAst();

            CfgNode currentNode = generateCFGForOneStatement(statement, beginStatementNode, endStatementNode, compilationUnit, firstLine, coverage);

            return currentNode;
        }

    }

    //Trong TH beforeNode là câu lệnh điều kiện boolean CfgBoolExprNode thì thenOrElse sẽ xác định ta gắn
    //câu lệnh mới vào afterNode hay falseNode của beforeNode
    //Hàm trả ra là Nút tương ứng với câu lệnh đầu tiên và một danh sách tương ứng với câu lệnh cuối cùng của
    // khối lệnh mà đứng trước nút End của khối
    public static CfgNode generateCFGForOneStatement(ASTNode statement, CfgNode beforeNode, CfgNode afterNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        CfgNode currentNode;

        if (statement instanceof SwitchStatement) {
            currentNode = new CfgSwitchStatementBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);

            CfgBeginSwitchNode beginSwitchNode = generateCFGFromSwitchASTNode((CfgSwitchStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);

            return beginSwitchNode;
        } else if (statement instanceof IfStatement) {
            currentNode = new CfgIfStatementBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);

            CfgBoolExprNode beginIfNode = generateCFGFromIfASTNodeForBranch_Statement_PathCoverage((CfgIfStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);

//            CfgBoolExprNode beginIfNode = null;
//
//            if(coverage == Coverage.BRANCH || coverage == Coverage.STATEMENT || coverage == Coverage.PATH) {
//                beginIfNode = generateCFGFromIfASTNodeForBranch_Statement_PathCoverage((CfgIfStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);
//            } else if (coverage == Coverage.MCDC) {
//                beginIfNode = generateCFGFromIfASTNodeForMCDCCoverage((CfgIfStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);
//            } else {
//                throw new RuntimeException("Invalid coverage!");
//            }

            return beginIfNode;
        } else if (statement instanceof ForStatement) {
            currentNode = new CfgForStatementBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);

            CfgBeginForNode beginForNode = generateCFGFromForASTNode((CfgForStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);

//            System.out.println("forRootNode = " + beginForNode.toString());

            return beginForNode;

        } else if (statement instanceof EnhancedForStatement) {
            //foreach statement
            currentNode = new CfgForEachStatementBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);

            CfgBeginForEachNode beginForEachNode =
                    generateCFGFromForEachASTNode((CfgForEachStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);

//            System.out.println("beginForEachNode = " + beginForEachNode.toString());

            return beginForEachNode;

        } else if (statement instanceof WhileStatement) {
            currentNode = new CfgWhileStatementBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);

            CfgBoolExprNode beginWhileNode = generateCFGFromWhileASTNode((CfgWhileStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);

//            System.out.println("beginWhileNode = " + beginWhileNode.toString());

            return beginWhileNode;

        } else if (statement instanceof DoStatement) {
            currentNode = new CfgDoStatementBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);

            CfgBeginDoNode beginDoNode = generateCFGFromDoASTNode((CfgDoStatementBlockNode) currentNode, compilationUnit, firstLine, coverage);

//            System.out.println("beginDoNode = " + beginDoNode.toString());

            return beginDoNode;
        } else if (statement instanceof Block) {
            currentNode = new CfgBlockNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);
            CfgBeginBlockNode cfgBeginBlockNode = generateCFGFromBlockASTNode((CfgBlockNode) currentNode, compilationUnit, firstLine, coverage);
            return cfgBeginBlockNode;
        } else if (statement instanceof ExpressionStatement) {
            currentNode = new CfgNormalNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);
        } else if (statement instanceof ReturnStatement) {
            currentNode = new CfgReturnStatementNode();
            currentNode.setAst(statement);

//            LinkCurrentNode(beforeNode, currentNode, afterNode);

            // new linking
            beforeNode.setAfterStatementNode(currentNode);
            currentNode.setBeforeStatementNode(beforeNode);
            currentNode.setAfterStatementNode(endCfgNode);
            endCfgNode.getBeforeEndBoolNodeList().add(currentNode);

            // for controlling "BeforeEndBoolNodeList"
            afterNode.setBeforeStatementNode(currentNode);
        } else if (statement instanceof VariableDeclarationStatement) {
            currentNode = new CfgNormalNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);
        } else if (statement instanceof BreakStatement) {
            currentNode = new CfgBreakStatementNode();
            currentNode.setAst(statement);

            // Linking
            beforeNode.setAfterStatementNode(currentNode);
            currentNode.setBeforeStatementNode(beforeNode);
            currentNode.setAfterStatementNode(endNodeStack.peek());
            ((CfgEndBlockNode) endNodeStack.peek()).getBeforeEndBoolNodeList().add(currentNode);

            // for controlling "BeforeEndBoolNodeList"
            afterNode.setBeforeStatementNode(currentNode);

        } else if (statement instanceof ContinueStatement) {
            currentNode = new CfgContinueStatementNode();
            currentNode.setAst(statement);

            // Linking
            beforeNode.setAfterStatementNode(currentNode);
            currentNode.setBeforeStatementNode(beforeNode);
            currentNode.setAfterStatementNode(conditionNodeStack.peek());
            // connect??

            // for controlling "BeforeEndBoolNodeList"
            afterNode.setBeforeStatementNode(currentNode);
        } else {
            currentNode = new CfgNormalNode();
            currentNode.setAst(statement);

            LinkCurrentNode(beforeNode, currentNode, afterNode);
        }

        setLineNumber(currentNode, compilationUnit, statement, firstLine);

        currentNode.setStartPosition(statement.getStartPosition());
        currentNode.setEndPosition(statement.getStartPosition() + statement.getLength());

        currentNode.setBeforeStatementNode(beforeNode);

        return currentNode;
    }

    private static void LinkCurrentNode(CfgNode beforeNode, CfgNode currentNode, CfgNode afterNode) {
        beforeNode.setAfterStatementNode(currentNode);
        currentNode.setBeforeStatementNode(beforeNode);

        currentNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(currentNode);

//        if (afterNode instanceof CfgEndBlockNode)
//        {
//            ((CfgEndBlockNode) afterNode).getBeforeEndBoolNodeList().add(currentNode);
//        }

    }

    // Just being used in "generateCFGFromIfASTNode"
    private static void addToBeforeEndBoolNodeList(CfgEndBlockNode cfgEndBlockNode) {
        CfgNode beforeNode = cfgEndBlockNode.getBeforeStatementNode();
        if (!(beforeNode instanceof CfgReturnStatementNode ||
                beforeNode instanceof CfgBreakStatementNode ||
                beforeNode instanceof CfgContinueStatementNode)) {
            cfgEndBlockNode.getBeforeEndBoolNodeList().add(cfgEndBlockNode.getBeforeStatementNode());
        }
    }

    public static CfgBeginSwitchNode generateCFGFromSwitchASTNode(CfgSwitchStatementBlockNode switchCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        // initialize
        CfgNode beforeNode = switchCfgNode.getBeforeStatementNode();
        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();
        CfgNode afterNode = switchCfgNode.getAfterStatementNode(); // outside switch block

        // connect end of switch block with outside switch block
        cfgEndBlockNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBlockNode);

        // add end node to keep track of latest endBlockNode
        endNodeStack.push(cfgEndBlockNode);

        // initialize the first node of switch CFG (beginSwitchNode)
        CfgBeginSwitchNode beginSwitchNode = new CfgBeginSwitchNode();

        // set content, AST, endBlockNode of beginSwitchNode
        Expression switchExpression = ((SwitchStatement) switchCfgNode.getAst()).getExpression();
        beginSwitchNode.setEndBlockNode(cfgEndBlockNode);
        beginSwitchNode.setAst(switchExpression);
        setLineNumber(beginSwitchNode, compilationUnit, switchExpression, firstLine);
        beginSwitchNode.setContent(switchExpression.toString());

        // connect switch statement with node before switch block
        beforeNode.setAfterStatementNode(beginSwitchNode);
        beginSwitchNode.setBeforeStatementNode(beforeNode);

        // get all statements in switch block iterate through them
        List<ASTNode> caseStatements = ((SwitchStatement) switchCfgNode.getAst()).statements();

        CfgNode previousNode = beginSwitchNode;
        CfgBoolExprNode previousCaseNode = null;

        for (int i = 0; i < caseStatements.size(); i++) {

            // Check if the statement is a case statement
            if (caseStatements.get(i) instanceof SwitchCase) {
                // Case condition expression
                CfgBoolExprNode caseExpression = new CfgBoolExprNode();
                caseExpression.setAst(caseStatements.get(i));
                setLineNumber(caseExpression, compilationUnit, caseStatements.get(i), firstLine);
                caseExpression.setContent(caseStatements.get(i).toString());

                // if the previous statement is a break statement
                if (previousNode instanceof CfgBreakStatementNode) {

                    // then link the case statement with the previous case statement
                    LinkCurrentNode(previousCaseNode, caseExpression, endNodeStack.peek());
                } else { // if the previous statement is NOT a break statement

                    // then just link the case statement with the previous node as normal
                    LinkCurrentNode(previousNode, caseExpression, endNodeStack.peek());
                }
                // set falseNode
                if (previousCaseNode != null) previousCaseNode.setFalseNode(caseExpression);

                // update
                previousCaseNode = caseExpression;
                previousNode = caseExpression;
            } else {

                CfgNode tmpNode = generateCFGForOneStatement(caseStatements.get(i), previousNode, endNodeStack.peek(), compilationUnit, firstLine, coverage);

                // if previous node is a case statement then the current node is its true node
                if (caseStatements.get(i - 1) instanceof SwitchCase) {
                    ((CfgBoolExprNode) previousNode).setTrueNode(tmpNode);
                }

                // update
                previousNode = tmpNode;
            }
        }

        if (previousCaseNode != null) previousCaseNode.setFalseNode(cfgEndBlockNode);

        endNodeStack.pop();

        return beginSwitchNode;
    }

    public static CfgBoolExprNode generateCFGFromIfASTNodeForBranch_Statement_PathCoverage(CfgIfStatementBlockNode ifCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        CfgNode beforeNode = ifCfgNode.getBeforeStatementNode();
        CfgEndBlockNode cfgEndBoolNode = new CfgEndBlockNode();

        CfgNode afterNode = ifCfgNode.getAfterStatementNode();

        cfgEndBoolNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBoolNode);

        //CfgNode ifConditionNode =
        Expression ifConditionAST = ((IfStatement) ifCfgNode.getAst()).getExpression();

        CfgBoolExprNode ifCondition = new CfgBoolExprNode();
        ifCondition.setAst(ifConditionAST);
        setLineNumber(ifCondition, compilationUnit, ifConditionAST, firstLine);
        ifCondition.setContent(ifConditionAST.toString());

        ifCondition.setBeforeStatementNode(beforeNode);
        beforeNode.setAfterStatementNode(ifCondition);

        Statement thenAST = ((IfStatement) ifCfgNode.getAst()).getThenStatement();
        CfgNode cfgThenNodeBlock = new CfgBlockNode();

        cfgThenNodeBlock.setAst(thenAST);
        setLineNumber(cfgThenNodeBlock, compilationUnit, thenAST, firstLine);
        cfgThenNodeBlock.setContent(thenAST.toString());

        cfgThenNodeBlock.setBeforeStatementNode(ifCondition);

        cfgThenNodeBlock.setAfterStatementNode(cfgEndBoolNode);

        CfgNode cfgThenNode = generateCFGFromASTBlockNode(cfgThenNodeBlock, compilationUnit, firstLine, coverage);

        // add to BeforeEndBoolNodeList
        addToBeforeEndBoolNodeList(cfgEndBoolNode);

        if (cfgThenNode == null) {
            ifCondition.setTrueNode(cfgEndBoolNode);
        } else {
            ifCondition.setTrueNode(cfgThenNode);
        }

        Statement elseAST = ((IfStatement) ifCfgNode.getAst()).getElseStatement();

        if (elseAST != null) {
            CfgNode cfgElseNodeBlock = new CfgBlockNode();
            cfgElseNodeBlock.setAst(elseAST);
            setLineNumber(cfgElseNodeBlock, compilationUnit, elseAST, firstLine);
            cfgElseNodeBlock.setContent(elseAST.toString());

            cfgElseNodeBlock.setBeforeStatementNode(ifCondition);

            cfgElseNodeBlock.setAfterStatementNode(cfgEndBoolNode);

            CfgNode cfgElseNode = generateCFGFromASTBlockNode(cfgElseNodeBlock, compilationUnit, firstLine, coverage);

            // add to BeforeEndBoolNodeList
            addToBeforeEndBoolNodeList(cfgEndBoolNode);

            ifCondition.setFalseNode(cfgElseNode);
        } else {
            ifCondition.setFalseNode(cfgEndBoolNode);
        }

        ifCondition.setEndBlockNode(cfgEndBoolNode);

        return ifCondition;
    }

    public static CfgBoolExprNode generateCFGFromIfASTNodeForMCDCCoverage(CfgIfStatementBlockNode ifCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        CfgNode beforeNode = ifCfgNode.getBeforeStatementNode();
        CfgEndBlockNode cfgEndBoolNode = new CfgEndBlockNode();

        CfgNode afterNode = ifCfgNode.getAfterStatementNode();

        cfgEndBoolNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBoolNode);

        // AST
        IfStatement ifStatement = (IfStatement) ifCfgNode.getAst();
        Expression ifConditionAST = ifStatement.getExpression();
        Statement thenAST = ifStatement.getThenStatement();
        Statement elseAST = ifStatement.getElseStatement();

        // tmpEndNode xử lí vấn đề liên quan đến độ phủ điều kiện con của các câu lệnh "Và" ('&&', '&')
        CfgEndBlockNode tmpEndNode = new CfgEndBlockNode();

        CfgBoolExprNode ifCondition = generateConditionCfg(ifConditionAST, cfgEndBoolNode, thenAST, beforeNode, tmpEndNode, compilationUnit, firstLine, coverage);

        CfgNode cfgElseNode = createElseBlock(elseAST, ifCondition, cfgEndBoolNode, compilationUnit, firstLine, coverage);

        tmpEndNode.setAfterStatementNode(cfgElseNode);

        ifCondition.setFalseNode(cfgElseNode);
        ifCondition.setEndBlockNode(cfgEndBoolNode);

        return ifCondition;
    }

    // tmpEndNode xử lí vấn đề liên quan đến độ phủ điều kiện con của các câu lệnh "Và" ('&&', '&')
    public static CfgBoolExprNode generateConditionCfg(Expression condition, CfgEndBlockNode endBoolNode, Statement thenStatement, CfgNode beforeNode, CfgEndBlockNode tmpEndNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {

        if (condition instanceof InfixExpression && isOrOperator(((InfixExpression) condition).getOperator())) { // điều kiện có chứa dấu "Hoặc"
            InfixExpression infixExpression = (InfixExpression) condition;
            Expression leftOperand = infixExpression.getLeftOperand();
            Expression rightOperand = infixExpression.getRightOperand();
            List extendedOperands = infixExpression.extendedOperands();

            CfgEndBlockNode firstTmpEndNode = new CfgEndBlockNode();
            CfgEndBlockNode currentTmpEndNode = new CfgEndBlockNode();

            CfgBoolExprNode firstCondition = generateConditionCfg(leftOperand, endBoolNode, thenStatement, beforeNode, firstTmpEndNode, compilationUnit, firstLine, coverage);
            CfgBoolExprNode currentCondition = generateConditionCfg(rightOperand, endBoolNode, thenStatement, firstCondition, currentTmpEndNode, compilationUnit, firstLine, coverage);

            firstTmpEndNode.setAfterStatementNode(currentCondition);

            firstCondition.setFalseNode(currentCondition);

            for (int i = 0; i < extendedOperands.size(); i++) {
                CfgEndBlockNode newTmpEndNode = new CfgEndBlockNode();
                CfgBoolExprNode newCondition = generateConditionCfg((Expression) extendedOperands.get(i), endBoolNode, thenStatement, currentCondition, newTmpEndNode, compilationUnit, firstLine, coverage);

                currentTmpEndNode.setAfterStatementNode(newCondition);
                currentTmpEndNode = newTmpEndNode;

                currentCondition.setFalseNode(newCondition);
                currentCondition = newCondition;
            }

            return currentCondition;

        } else if (condition instanceof InfixExpression && isAndOperator(((InfixExpression) condition).getOperator())) { // điều kiện có chứa dấu "Và"
            InfixExpression infixExpression = (InfixExpression) condition;
            Expression leftOperand = infixExpression.getLeftOperand();
            Expression rightOperand = infixExpression.getRightOperand();
            List extendedOperands = infixExpression.extendedOperands();

            CfgBoolExprNode firstCondition = createCondition(leftOperand, beforeNode, compilationUnit, firstLine);

            CfgBoolExprNode currentCondition = createCondition(rightOperand, firstCondition, compilationUnit, firstLine);
            firstCondition.setupCondition(currentCondition, tmpEndNode, endBoolNode);

            for (int i = 0; i < extendedOperands.size(); i++) {
                CfgBoolExprNode newCondition = createCondition((Expression) extendedOperands.get(i), currentCondition, compilationUnit, firstLine);
                currentCondition.setupCondition(newCondition, tmpEndNode, endBoolNode);
                currentCondition = newCondition;
            }

            currentCondition.setupCondition(createThenBlock(thenStatement, currentCondition, endBoolNode, compilationUnit, firstLine, coverage), tmpEndNode, endBoolNode);

            return firstCondition;

        } else {

            CfgBoolExprNode ifCondition = createCondition(condition, beforeNode, compilationUnit, firstLine);

            ifCondition.setTrueNode(createThenBlock(thenStatement, ifCondition, endBoolNode, compilationUnit, firstLine, coverage));

            ifCondition.setEndBlockNode(endBoolNode);

            return ifCondition;
        }
    }

    private static CfgBoolExprNode createCondition(Expression condition, CfgNode beforeNode, CompilationUnit compilationUnit, int firstLine) {
        CfgBoolExprNode ifCondition = new CfgBoolExprNode();
        ifCondition.setAst(condition);
        setLineNumber(ifCondition, compilationUnit, condition, firstLine);
        ifCondition.setContent(condition.toString());

        ifCondition.setBeforeStatementNode(beforeNode);
        beforeNode.setAfterStatementNode(ifCondition);

        return ifCondition;
    }

    private static CfgNode createThenBlock(Statement thenStatement, CfgBoolExprNode condition, CfgEndBlockNode endBoolNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        CfgNode cfgThenNodeBlock = new CfgBlockNode();

        cfgThenNodeBlock.setAst(thenStatement);
        setLineNumber(cfgThenNodeBlock, compilationUnit, thenStatement, firstLine);
        cfgThenNodeBlock.setContent(thenStatement.toString());

        cfgThenNodeBlock.setBeforeStatementNode(condition);

        cfgThenNodeBlock.setAfterStatementNode(endBoolNode);

        CfgNode cfgThenNode = generateCFGFromASTBlockNode(cfgThenNodeBlock, compilationUnit, firstLine, coverage);

        // add to BeforeEndBoolNodeList
        addToBeforeEndBoolNodeList(endBoolNode);

        if (cfgThenNode == null) {
            return endBoolNode;
        } else {
            return cfgThenNode;
        }
    }

    private static CfgNode createElseBlock(Statement elseStatement, CfgBoolExprNode condition, CfgEndBlockNode endBoolNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        if (elseStatement != null) {
            CfgNode cfgElseNodeBlock = new CfgBlockNode();
            cfgElseNodeBlock.setAst(elseStatement);
            setLineNumber(cfgElseNodeBlock, compilationUnit, elseStatement, firstLine);
            cfgElseNodeBlock.setContent(elseStatement.toString());

            cfgElseNodeBlock.setBeforeStatementNode(condition);

            cfgElseNodeBlock.setAfterStatementNode(endBoolNode);

            CfgNode cfgElseNode = generateCFGFromASTBlockNode(cfgElseNodeBlock, compilationUnit, firstLine, coverage);

            // add to BeforeEndBoolNodeList
            addToBeforeEndBoolNodeList(endBoolNode);

            return cfgElseNode;
        } else {
            return endBoolNode;
        }
    }

    private static boolean isOrOperator(InfixExpression.Operator operator) {
        return operator.equals(InfixExpression.Operator.CONDITIONAL_OR) ||
                operator.equals(InfixExpression.Operator.OR);
    }

    private static boolean isAndOperator(InfixExpression.Operator operator) {
        return operator.equals(InfixExpression.Operator.CONDITIONAL_AND) ||
                operator.equals(InfixExpression.Operator.AND);
    }

    private static CfgBeginBlockNode generateCFGFromBlockASTNode(CfgBlockNode cfgBlockNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
        CfgNode beforeNode = cfgBlockNode.getBeforeStatementNode();
        CfgBeginBlockNode beginBlockNode = new CfgBeginBlockNode();

        beforeNode.setAfterStatementNode(beginBlockNode);
        beginBlockNode.setBeforeStatementNode(beforeNode);

        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();
        CfgNode afterNode = cfgBlockNode.getAfterStatementNode();

        cfgEndBlockNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBlockNode);

        beginBlockNode.setEndBlockNode(cfgEndBlockNode);
        cfgBlockNode.setAfterStatementNode(cfgEndBlockNode);
        cfgBlockNode.setBeforeStatementNode(beginBlockNode);

        CfgNode bodyNode = generateCFGFromASTBlockNode(cfgBlockNode, compilationUnit, firstLine, coverage);

        if (bodyNode != null) {
            beginBlockNode.setAfterStatementNode(bodyNode);
        } else {
            beginBlockNode.setAfterStatementNode(cfgEndBlockNode);
        }

        return beginBlockNode;
    }

//    public static CfgBeginForNode generateCFGFromForASTNode(CfgForStatementBlockNode forCfgNode) {
////        System.out.println("generateCFGFromForASTNode starts...");
//        CfgNode beforeNode = forCfgNode.getBeforeStatementNode();
//        CfgBeginForNode beginForNode = new CfgBeginForNode();
//
//        beforeNode.setAfterStatementNode(beginForNode);
//        beginForNode.setBeforeStatementNode(beforeNode);
//
//        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();
//
//        CfgNode afterNode = forCfgNode.getAfterStatementNode();
//
//        cfgEndBlockNode.setAfterStatementNode(afterNode);
//        afterNode.setBeforeStatementNode(cfgEndBlockNode);
//
//        beginForNode.setEndBlockNode(cfgEndBlockNode);
//
//        List initializers = ((ForStatement) forCfgNode.getAst()).initializers();
//
//        CfgNode tempBeforeNode = beginForNode;
//
//        for (int i = 0; i < initializers.size(); i++) {
//            CfgNormalNode normalNode = new CfgNormalNode();
//
//            if (initializers.get(i) instanceof VariableDeclarationExpression) {
//                normalNode.setAst((VariableDeclarationExpression) initializers.get(i));
//            } else if (initializers.get(i) instanceof Assignment) {
//                normalNode.setAst((Assignment) initializers.get(i));
//            }
//
//            LinkCurrentNode(tempBeforeNode, normalNode, afterNode);
//
//            tempBeforeNode = normalNode;
//        }
//
//        //Dieu kien
//        Expression forConditionAST = ((ForStatement) forCfgNode.getAst()).getExpression();
//
////        CfgBoolExprNode forConditionNode = new CfgBoolExprNode();
////        forConditionNode.setAst(forConditionAST);
////        forConditionNode.setContent(forConditionAST.toString());
////
////        LinkCurrentNode(tempBeforeNode, forConditionNode, afterNode);
//
//        //Khoi body
//        Statement bodyStatementBlock = ((ForStatement) forCfgNode.getAst()).getBody();
////        CfgNode bodyStatementNode = new CfgBlockNode();
////        bodyStatementNode.setAst(bodyStatementBlock);
////        bodyStatementNode.setContent(bodyStatementBlock.toString());
//
//        CfgEndBlockNode endBodyBlockNode = new CfgEndBlockNode();
//
//        //Updater
//        List updaters = ((ForStatement) forCfgNode.getAst()).updaters();
//
//        CfgNode tempBeforeUpdaterNode = endBodyBlockNode;
//
//        CfgNode firstUpdaterNode = null;
//
//        for (int i = 0; i < updaters.size(); i++) {
//            CfgNormalNode normalNode = new CfgNormalNode();
//
//            if (updaters.get(i) instanceof PostfixExpression) {
//                normalNode.setAst((PostfixExpression) updaters.get(i));
//            } else if (updaters.get(i) instanceof Assignment) {
//                normalNode.setAst((Assignment) updaters.get(i));
//            }
//
//            LinkCurrentNode(tempBeforeUpdaterNode, normalNode, afterNode);
//
//            tempBeforeUpdaterNode = normalNode;
//
//            if (i == 0) {
//                firstUpdaterNode = normalNode;
//            }
//        }
//
//        endBodyBlockNode.setAfterStatementNode(firstUpdaterNode);
//        firstUpdaterNode.setBeforeStatementNode(endBodyBlockNode);
//
//        // add end node to keep track of latest endBlockNode
//        endNodeStack.push(cfgEndBlockNode);
//
//        // add condition node to keep track of the latest condition node
//        conditionNodeStack.push(endBodyBlockNode);
//
////        bodyStatementNode.setBeforeStatementNode(forConditionNode);
////        bodyStatementNode.setAfterStatementNode(endBodyBlockNode);
//
//        // here
//        CfgEndBlockNode tmpEndNode = new CfgEndBlockNode();
//
//        CfgBoolExprNode forConditionNode = generateConditionCfg(forConditionAST, endBodyBlockNode, bodyStatementBlock, tempBeforeUpdaterNode, tmpEndNode);
//        tmpEndNode.setAfterStatementNode(cfgEndBlockNode);
//
//        tempBeforeNode.setAfterStatementNode(forConditionNode);
//        tempBeforeUpdaterNode.setAfterStatementNode(forConditionNode);
//
////        CfgNode cfgBodyNode = generateCFGFromASTBlockNode(bodyStatementNode);
//
//
//
////        if (cfgBodyNode == null) {
////            forConditionNode.setTrueNode(endBodyBlockNode);
////        } else {
////            forConditionNode.setTrueNode(cfgBodyNode);
////        }
//        forConditionNode.setFalseNode(cfgEndBlockNode);
//
//        cfgEndBlockNode.getBeforeEndBoolNodeList().add(forConditionNode);
//        cfgEndBlockNode.setBeforeStatementNode(forConditionNode);
//
//        // pop from stack to delete finished "for" block
//        endNodeStack.pop();
//        conditionNodeStack.pop();
//
//        return beginForNode;
//    }

    public static CfgBeginForNode generateCFGFromForASTNode(CfgForStatementBlockNode forCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
//        System.out.println("generateCFGFromForASTNode starts...");
        CfgNode beforeNode = forCfgNode.getBeforeStatementNode();
        CfgBeginForNode beginForNode = new CfgBeginForNode();

        beforeNode.setAfterStatementNode(beginForNode);
        beginForNode.setBeforeStatementNode(beforeNode);

        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();

        CfgNode afterNode = forCfgNode.getAfterStatementNode();

        cfgEndBlockNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBlockNode);

        beginForNode.setEndBlockNode(cfgEndBlockNode);

        List initializers = ((ForStatement) forCfgNode.getAst()).initializers();

        CfgNode tempBeforeNode = beginForNode;

        for (int i = 0;
             i < initializers.size();
             i++) {
            CfgNormalNode normalNode = new CfgNormalNode();

            if (initializers.get(i) instanceof VariableDeclarationExpression) {
                normalNode.setAst((VariableDeclarationExpression) initializers.get(i));
                setLineNumber(normalNode, compilationUnit, (VariableDeclarationExpression) initializers.get(i), firstLine);
            } else if (initializers.get(i) instanceof Assignment) {
                normalNode.setAst((Assignment) initializers.get(i));
                setLineNumber(normalNode, compilationUnit, (Assignment) initializers.get(i), firstLine);
            }

            LinkCurrentNode(tempBeforeNode, normalNode, afterNode);

            tempBeforeNode = normalNode;
        }

        //Dieu kien
        Expression forConditionAST = ((ForStatement) forCfgNode.getAst()).getExpression();

        CfgBoolExprNode forConditionNode = new CfgBoolExprNode();
        forConditionNode.setAst(forConditionAST);
        setLineNumber(forConditionNode, compilationUnit, forConditionAST, firstLine);
        forConditionNode.setContent(forConditionAST.toString());

        LinkCurrentNode(tempBeforeNode, forConditionNode, afterNode);

        //Khoi body
        Statement bodyStatementBlock = ((ForStatement) forCfgNode.getAst()).getBody();
        CfgNode bodyStatementNode = new CfgBlockNode();
        bodyStatementNode.setAst(bodyStatementBlock);
        setLineNumber(bodyStatementNode, compilationUnit, bodyStatementBlock, firstLine);
        bodyStatementNode.setContent(bodyStatementBlock.toString());

        //Updater
        List updaters = ((ForStatement) forCfgNode.getAst()).updaters();

        CfgNode tempBeforeUpdaterNode = bodyStatementNode;

        CfgNode firstUpdaterNode = null;

        for (int i = 0; i < updaters.size(); i++) {
            CfgNormalNode normalNode = new CfgNormalNode();

            if (updaters.get(i) instanceof PostfixExpression) {
                normalNode.setAst((PostfixExpression) updaters.get(i));
                setLineNumber(normalNode, compilationUnit, (PostfixExpression) updaters.get(i), firstLine);
            } else if (updaters.get(i) instanceof Assignment) {
                normalNode.setAst((Assignment) updaters.get(i));
                setLineNumber(normalNode, compilationUnit, (Assignment) updaters.get(i), firstLine);
            }

            LinkCurrentNode(tempBeforeUpdaterNode, normalNode, afterNode);

            tempBeforeUpdaterNode = normalNode;

            if (i == 0) {
                firstUpdaterNode = normalNode;
            }
        }

        tempBeforeUpdaterNode.setAfterStatementNode(forConditionNode);

        // add end node to keep track of latest endBlockNode
        endNodeStack.push(cfgEndBlockNode);

        CfgEndBlockNode endBodyBlockNode = new CfgEndBlockNode();

        bodyStatementNode.setBeforeStatementNode(forConditionNode);
        bodyStatementNode.setAfterStatementNode(endBodyBlockNode);

        endBodyBlockNode.setAfterStatementNode(firstUpdaterNode);
        firstUpdaterNode.setBeforeStatementNode(endBodyBlockNode);

        // add condition node to keep track of the latest condition node
        conditionNodeStack.push(endBodyBlockNode);

        CfgNode cfgBodyNode = generateCFGFromASTBlockNode(bodyStatementNode, compilationUnit, firstLine, coverage);

        // pop from stack to delete finished "for" block
        endNodeStack.pop();
        conditionNodeStack.pop();

        if (cfgBodyNode == null) {
            forConditionNode.setTrueNode(endBodyBlockNode);
        } else {
            forConditionNode.setTrueNode(cfgBodyNode);
        }
        forConditionNode.setFalseNode(cfgEndBlockNode);

        cfgEndBlockNode.getBeforeEndBoolNodeList().add(forConditionNode);
        cfgEndBlockNode.setBeforeStatementNode(forConditionNode);

//        System.out.println("generateCFGFromForASTNode ends...");

        return beginForNode;
    }

    public static CfgBoolExprNode generateCFGFromWhileASTNode(CfgWhileStatementBlockNode whileCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
//        System.out.println("generateCFGFromWhileASTNode starts...");
        CfgNode beforeNode = whileCfgNode.getBeforeStatementNode();

        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();

        CfgNode afterNode = whileCfgNode.getAfterStatementNode();

        cfgEndBlockNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBlockNode);

        //Dieu kien
        Expression whileConditionAST = ((WhileStatement) whileCfgNode.getAst()).getExpression();

        CfgBoolExprNode whileConditionNode = new CfgBoolExprNode();
        whileConditionNode.setAst(whileConditionAST);
        setLineNumber(whileConditionNode, compilationUnit, whileConditionAST, firstLine);
        whileConditionNode.setContent(whileConditionAST.toString());

        whileConditionNode.setEndBlockNode(cfgEndBlockNode);
        beforeNode.setAfterStatementNode(whileConditionNode);
        whileConditionNode.setBeforeStatementNode(beforeNode);

        //Khoi body
        Statement bodyStatementBlock = ((WhileStatement) whileCfgNode.getAst()).getBody();
        CfgNode bodyStatementNode = new CfgBlockNode();

        bodyStatementNode.setAst(bodyStatementBlock);
        setLineNumber(bodyStatementNode, compilationUnit, bodyStatementBlock, firstLine);
        bodyStatementNode.setContent(bodyStatementBlock.toString());

        CfgEndBlockNode endBodyBlockNode = new CfgEndBlockNode();

        // add end node to keep track of latest endBlockNode
        endNodeStack.push(cfgEndBlockNode);

        bodyStatementNode.setAfterStatementNode(endBodyBlockNode);
        bodyStatementNode.setBeforeStatementNode(whileConditionNode);

        endBodyBlockNode.setAfterStatementNode(whileConditionNode);

        // add condition node to keep track of the latest condition node
        conditionNodeStack.push(endBodyBlockNode);

        CfgNode cfgBodyNode = generateCFGFromASTBlockNode(bodyStatementNode, compilationUnit, firstLine, coverage);

        // pop from stack to delete finished "while" block
        endNodeStack.pop();
        conditionNodeStack.pop();

        if (cfgBodyNode != null) {
            whileConditionNode.setTrueNode(cfgBodyNode);
        } else {
            whileConditionNode.setTrueNode(endBodyBlockNode);
        }
        whileConditionNode.setFalseNode(cfgEndBlockNode);

        cfgEndBlockNode.getBeforeEndBoolNodeList().add(whileConditionNode);
        cfgEndBlockNode.setBeforeStatementNode(whileConditionNode);

//        System.out.println("generateCFGFromWhileASTNode ends...");

        return whileConditionNode;
    }

    public static CfgBeginDoNode generateCFGFromDoASTNode(CfgDoStatementBlockNode doCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
//        System.out.println("generateCFGFromDoASTNode starts...");
        CfgNode beforeNode = doCfgNode.getBeforeStatementNode();

        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();

        CfgNode afterNode = doCfgNode.getAfterStatementNode();

        cfgEndBlockNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBlockNode);

        CfgBeginDoNode beginDoNode = new CfgBeginDoNode();
        beginDoNode.setBeforeStatementNode(beforeNode);
        beforeNode.setAfterStatementNode(beginDoNode);

        beginDoNode.setEndBlockNode(cfgEndBlockNode);

        //Khoi body
        Statement bodyStatementBlock = ((DoStatement) doCfgNode.getAst()).getBody();
        CfgNode bodyStatementNode = new CfgBlockNode();

        bodyStatementNode.setAst(bodyStatementBlock);
        setLineNumber(bodyStatementNode, compilationUnit, bodyStatementBlock, firstLine);
        bodyStatementNode.setContent(bodyStatementBlock.toString());

        //Dieu kien
        Expression doConditionAST = ((DoStatement) doCfgNode.getAst()).getExpression();

        CfgBoolExprNode doConditionNode = new CfgBoolExprNode();
        doConditionNode.setAst(doConditionAST);
        setLineNumber(doConditionNode, compilationUnit, doConditionAST, firstLine);
        doConditionNode.setContent(doConditionAST.toString());

        doConditionNode.setEndBlockNode(cfgEndBlockNode);

        CfgEndBlockNode endBodyBlockNode = new CfgEndBlockNode();

        // add end node to keep track of latest endBlockNode
        endNodeStack.push(cfgEndBlockNode);

        bodyStatementNode.setAfterStatementNode(endBodyBlockNode);
        bodyStatementNode.setBeforeStatementNode(beginDoNode);

        endBodyBlockNode.setAfterStatementNode(doConditionNode);
        doConditionNode.setBeforeStatementNode(endBodyBlockNode);

        // add condition node to keep track of the latest condition node
        conditionNodeStack.push(endBodyBlockNode);

        CfgNode cfgBodyNode = generateCFGFromASTBlockNode(bodyStatementNode, compilationUnit, firstLine, coverage);

        // pop from stack to delete finished "do-while" block
        endNodeStack.pop();

        if (cfgBodyNode != null) {
            doConditionNode.setTrueNode(cfgBodyNode);
        } else {
            doConditionNode.setTrueNode(endBodyBlockNode);
        }
        doConditionNode.setFalseNode(cfgEndBlockNode);

        cfgEndBlockNode.getBeforeEndBoolNodeList().add(doConditionNode);
        cfgEndBlockNode.setBeforeStatementNode(doConditionNode);

//        System.out.println("generateCFGFromDoASTNode ends...");

        return beginDoNode;
    }

    public static CfgBeginForEachNode generateCFGFromForEachASTNode(CfgForEachStatementBlockNode forEachCfgNode, CompilationUnit compilationUnit, int firstLine, Coverage coverage) {
//        System.out.println("generateCFGFromForEachASTNode starts...");
        CfgNode beforeNode = forEachCfgNode.getBeforeStatementNode();

        CfgEndBlockNode cfgEndBlockNode = new CfgEndBlockNode();

        CfgNode afterNode = forEachCfgNode.getAfterStatementNode();

        cfgEndBlockNode.setAfterStatementNode(afterNode);
        afterNode.setBeforeStatementNode(cfgEndBlockNode);

        CfgBeginForEachNode beginForEachNode = new CfgBeginForEachNode();
        beginForEachNode.setBeforeStatementNode(beforeNode);
        beforeNode.setAfterStatementNode(beginForEachNode);

        beginForEachNode.setEndBlockNode(cfgEndBlockNode);

        //Khoi expression
        Expression expressionAST = ((EnhancedForStatement) forEachCfgNode.getAst()).getExpression();
        CfgForEachExpressionNode expressionNode = new CfgForEachExpressionNode();
        expressionNode.setAst(expressionAST);
        setLineNumber(expressionNode, compilationUnit, expressionAST, firstLine);

        //Khoi parameter
        SingleVariableDeclaration parameterAST = ((EnhancedForStatement) forEachCfgNode.getAst()).getParameter();
        CfgNormalNode parameterNode = new CfgNormalNode();
        parameterNode.setAst(parameterAST);
        setLineNumber(parameterNode, compilationUnit, parameterAST, firstLine);

        beginForEachNode.setAfterStatementNode(expressionNode);
        expressionNode.setBeforeStatementNode(beginForEachNode);
        expressionNode.setParameterNode(parameterNode);

        //Khoi body
        Statement bodyStatementBlock = ((EnhancedForStatement) forEachCfgNode.getAst()).getBody();
        CfgNode bodyStatementNode = new CfgBlockNode();

        bodyStatementNode.setAst(bodyStatementBlock);
        setLineNumber(bodyStatementNode, compilationUnit, bodyStatementBlock, firstLine);
        bodyStatementNode.setContent(bodyStatementBlock.toString());

//        LinkCurrentNode(expressionNode, bodyStatementNode, expressionNode);

        CfgEndBlockNode endBodyBlockNode = new CfgEndBlockNode();

        // add end node to keep track of latest endBlockNode
        endNodeStack.push(cfgEndBlockNode);

        bodyStatementNode.setBeforeStatementNode(expressionNode);
        bodyStatementNode.setAfterStatementNode(endBodyBlockNode);

        endBodyBlockNode.setAfterStatementNode(expressionNode);

        // add condition node to keep track of the latest condition node
        conditionNodeStack.push(endBodyBlockNode);

        CfgNode cfgBodyNode = generateCFGFromASTBlockNode(bodyStatementNode, compilationUnit, firstLine, coverage);

        // pop from stack to delete finished "for-each" block
        endNodeStack.pop();
        conditionNodeStack.pop();

        if (cfgBodyNode != null) {
            expressionNode.setHasElementAfterNode(cfgBodyNode);
        } else {
            expressionNode.setHasElementAfterNode(endBodyBlockNode);
        }
        expressionNode.setNoMoreElementAfterNode(cfgEndBlockNode);

        cfgEndBlockNode.getBeforeEndBoolNodeList().add(expressionNode);
        cfgEndBlockNode.setBeforeStatementNode(expressionNode);

//        System.out.println("generateCFGFromDoASTNode ends...");

        return beginForEachNode;
    }

    private static void setLineNumber(CfgNode cfgNode, CompilationUnit compilationUnit, ASTNode ast, int firstLine) {
        cfgNode.setLineNumber(compilationUnit.getLineNumber(ast.getStartPosition()) - firstLine);
    }
}
