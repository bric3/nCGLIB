package net.sf.cglib.transform;

import net.sf.cglib.core.*;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

public class AddPropertyTransformer extends EmittingTransformer {
    private final String[] names;
    private final Type[] types;

    public AddPropertyTransformer(String[] names, Type[] types) {
        this.names = names;
        this.types = types;
    }

    public void end_class() {
        if (!TypeUtils.isAbstract(getAccess())) {
            Type[] T = new Type[1];
            CodeEmitter e;
            for (int i = 0; i < names.length; i++) {
                String fieldName = "$cglib_prop_" + names[i];
                declare_field(Constants.ACC_PRIVATE, fieldName, types[i], null);

                String property = TypeUtils.upperFirst(names[i]);
                e = begin_method(Constants.ACC_PUBLIC,
                                 new Signature("get" + property,
                                               types[i],
                                               Constants.TYPES_EMPTY),
                                 null);
                e.load_this();
                e.getfield(fieldName);
                e.return_value();
                e.end_method();

                T[0] = types[i];
                e = begin_method(Constants.ACC_PUBLIC,
                                 new Signature("set" + property,
                                               Type.VOID_TYPE,
                                               T),
                                 null);
                e.load_this();
                e.load_arg(0);
                e.putfield(fieldName);
                e.return_value();
                e.end_method();
            }
        }
        super.end_class();
    }
}
