/*
 *
 *
 * $Id: AbstractTransformTest.java,v 1.5 2003-12-06 19:46:50 herbyderby Exp $
 */

package net.sf.cglib.transform;

import junit.framework.*;
import net.sf.cglib.core.CodeGenerationException;
/**
 *
 * @author  baliuka
 */
abstract public class AbstractTransformTest  extends TestCase{
   
    
     /** Creates a new instance of AbstractTransformTest */
    public AbstractTransformTest() {
        super(null);
    }
   
    /** Creates a new instance of AbstractTransformTest */
    public AbstractTransformTest(String s) {
       super(s);
    }
    
    protected abstract ClassTransformerFactory getTransformer()throws Exception;
    
     public Class transform()throws Exception{
       
       
       ClassLoader loader = new TransformingClassLoader(
          AbstractTransformTest.class.getClassLoader(),
          new ClassFilter(){
            public boolean accept(String name){
                
                return ! name.startsWith("java") &&
                       ! name.startsWith("junit") &&
                       ! name.endsWith("Exclude");
            } 
         },
         getTransformer()
        );
        
        
        return loader.loadClass(getClass().getName());
        
       
    
     }
    
    
}
