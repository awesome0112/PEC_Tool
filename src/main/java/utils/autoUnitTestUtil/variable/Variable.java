package utils.autoUnitTestUtil.variable;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public abstract class Variable {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Expr createZ3Variable(Variable variable, Context ctx){
        if(variable instanceof PrimitiveTypeVariable) {
            return PrimitiveTypeVariable.createZ3PrimitiveTypeVariable((PrimitiveTypeVariable) variable, ctx);
        } else if(variable instanceof ArrayTypeVariable) {
            throw new RuntimeException("Invalid type");
        } else {
            throw new RuntimeException("Invalid type");
        }
    }
}
