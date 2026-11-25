package org.spiderflow.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.annotation.Return;

public class Grammer {
	
	private String owner;

	private String method;
	
	private String comment;
	
	private String example;
	
	private String function;
	
	private List<String> returns;

	private List<String> parameters;
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public List<String> getReturns() {
		return returns;
	}
	
	public void setReturns(List<String> returns) {
		this.returns = returns;
	}

	public List<String> getParameters() {
		return parameters;
	}
	
	public static List<Grammer> findGrammers(Class<?> clazz,String function,String owner,boolean mustStatic){
		return findGrammers(clazz, function, owner, mustStatic, 0);
	}

	public static List<Grammer> findGrammers(Class<?> clazz,String function,String owner,boolean mustStatic,int firstParameter){
		Method[] methods = clazz.getDeclaredMethods();
		List<Grammer> grammers = new ArrayList<>();
		for (Method method : methods) {
			if(Modifier.isPublic(method.getModifiers()) && (Modifier.isStatic(method.getModifiers())||!mustStatic)){
				Grammer grammer = new Grammer();
				grammer.setMethod(method.getName());
				grammer.parameters = new ArrayList<>();
				Parameter[] parameters = method.getParameters();
				for (int i = firstParameter; i < parameters.length; i++) {
					Parameter parameter = parameters[i];
					String type = parameter.getType().getSimpleName();
					grammer.parameters.add(parameter.isNamePresent() ? type + " " + parameter.getName() : type);
				}
				Comment comment = method.getAnnotation(Comment.class);
				if(comment != null){
					grammer.setComment(comment.value());
				}
				Example example = method.getAnnotation(Example.class);
				if(example != null){
					grammer.setExample(example.value());
				}
				Return returns = method.getAnnotation(Return.class);
				if(returns != null){
					Class<?>[] clazzs = returns.value();
					List<String> returnTypes = new ArrayList<>();
					for (int i = 0; i < clazzs.length; i++) {
						returnTypes.add(clazzs[i].getSimpleName());
					}
					grammer.setReturns(returnTypes);
				}else{
					grammer.setReturns(Collections.singletonList(method.getReturnType().getSimpleName()));
				}
				grammer.setFunction(function);
				grammer.setOwner(owner);
				grammers.add(grammer);
			}
		}
		return grammers;
	}
}
