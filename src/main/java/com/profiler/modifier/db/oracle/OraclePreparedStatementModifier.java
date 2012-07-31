package com.profiler.modifier.db.oracle;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class OraclePreparedStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(OraclePreparedStatementModifier.class);

	public OraclePreparedStatementModifier(ClassPool classPool) {
		super(classPool);
	}
	
	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classLoader, javassistClassName);
		return changeMethod(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateSetInternalMethod(cc);
			updateExecuteMethod(cc);
			updateConstructor(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private void updateSetInternalMethod(CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[2];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.String");
		CtMethod serviceMethod1 = cc.getDeclaredMethod("setStringInternal", params1);

		serviceMethod1.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$2); }");

		// CtClass[] params2 = new CtClass[2];
		// params2[0] = classPool.getCtClass("int");
		// params2[1] = classPool.getCtClass("byte[]");
		// CtMethod serviceMethod2 = cc.getDeclaredMethod("setInternal",
		// params2);
		//
		// serviceMethod2.insertBefore("{" +
		// TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER +
		// ".putSqlParam($1,$2); {");
	}

	private void updateConstructor(CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();

		for (CtConstructor constructor : constructorList) {
			CtClass params[] = constructor.getParameterTypes();
			if (params.length == 6) {
				constructor.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
			}
		}
	}

	private void updateExecuteMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("execute", null);
		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
