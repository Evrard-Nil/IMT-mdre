package tp.fil.main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.modisco.java.emf.JavaPackage;
import org.eclipse.modisco.java.emf.impl.PackageImpl;
import org.eclipse.modisco.java.ASTNode;
import org.eclipse.modisco.java.BodyDeclaration;
import org.eclipse.modisco.java.ClassDeclaration;
import org.eclipse.modisco.java.Comment;
import org.eclipse.modisco.java.ConstructorDeclaration;
import org.eclipse.modisco.java.FieldDeclaration;
import org.eclipse.modisco.java.MethodDeclaration;
import org.eclipse.modisco.java.Model;
import org.eclipse.modisco.java.Package;
import org.eclipse.modisco.java.TypeDeclaration;
//import org.eclipse.modisco.java.Package;
//import org.eclipse.modisco.java.AbstractTypeDeclaration;
//import org.eclipse.modisco.java.BodyDeclaration;
//import org.eclipse.modisco.java.ClassDeclaration;
//import org.eclipse.modisco.java.FieldDeclaration;

public class DataComputation {

	private static EClass modelClass;
	private static EClass packageClass;
	private static EClass classDeclarationClass;
	private static EClass commentsClass;
	private static EClass methodDeclerationClass;
	private static EClass constructorDeclarationClass;
	private static EClass fieldDeclarationClass;
	private static EPackage dataPackage;

	public static void main(String[] args) {
		try {
			Resource javaModel;
			Resource dataModel;
			Resource dataMetamodel;

			// Create and configure resource set
			ResourceSet resSet = new ResourceSetImpl();
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

			// Load Java & Data metamodel
			JavaPackage.eINSTANCE.eClass();

			dataMetamodel = resSet.createResource(URI.createFileURI("src/tp/fil/resources/Data.ecore"));
			dataMetamodel.load(null);
			EPackage.Registry.INSTANCE.put("http://data", dataMetamodel.getContents().get(0));

			// Load Java model
			javaModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_java.xmi"));
			javaModel.load(null);

			// Initiate Data model with a "Model" root object/element
			dataModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_data.xmi"));

			dataPackage = (EPackage) dataMetamodel.getContents().get(0);

			modelClass = (EClass) dataPackage.getEClassifier("Model");
			packageClass = (EClass) dataPackage.getEClassifier("Package");
			commentsClass = (EClass) dataPackage.getEClassifier("Comment");
			classDeclarationClass = (EClass) dataPackage.getEClassifier("Classe");
			fieldDeclarationClass = (EClass) dataPackage.getEClassifier("FieldDeclaration");
			methodDeclerationClass = (EClass) dataPackage.getEClassifier("MethodDeclaration");
			constructorDeclarationClass = (EClass) dataPackage.getEClassifier("ConstructorDeclaration");

			EObject modelObject = dataPackage.getEFactoryInstance().create(modelClass);

			Model javaModelRootElement = (Model) javaModel.getContents().get(0);
			modelObject.eSet(modelClass.getEStructuralFeature("name"), javaModelRootElement.getName());

			dataModel.getContents().add(modelObject);

			// Build the actual Data model by navigating the Java model
			// and creating the appropriate Data model elements...
			TreeIterator<EObject> iterator = javaModel.getAllContents();

			/*
			 * Beginning of the part to be completed...
			 */
			while (iterator.hasNext()) {
				EObject currentModelElement = iterator.next();
				if (currentModelElement.eClass().getName().equals("Model")) {
					transfoModel(modelObject, currentModelElement);

				}
			}

			// TO BE COMPLETED

			/*
			 * End of the part to be completed...
			 */

			// Serialize Data model
			dataModel.save(new FileOutputStream("PetStore_JavaOutput.xmi"), null);

			// Unload models
			javaModel.unload();
			dataModel.unload();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void transfoModel(EObject modelObject, EObject currentModelElement) {
		EStructuralFeature nameFeat = currentModelElement.eClass().getEStructuralFeature("name");
		EStructuralFeature nameFeatD = modelClass.getEStructuralFeature("name");

		String name = (String) currentModelElement.eGet(nameFeat);

		modelObject.eSet(nameFeatD, name);

		EStructuralFeature ownedElementsFeature = currentModelElement.eClass().getEStructuralFeature("ownedElements");
		EStructuralFeature ownedElementsFeatureD = modelClass.getEStructuralFeature("ownedElements");
		List<Package> ownedElements = (List<Package>) currentModelElement.eGet(ownedElementsFeature);
		List<EObject> ownedElementsD = transfoPackage(ownedElements);
		modelObject.eSet(ownedElementsFeatureD, ownedElementsD);
	}

	private static List<EObject> transfoPackage(List<Package> packages) {
		List<EObject> packagesD = new ArrayList<>();
		for (Package p : packages) {
			EObject packageObject = dataPackage.getEFactoryInstance().create(packageClass);
			packageObject.eSet(packageClass.getEStructuralFeature("name"), p.getName());

			EStructuralFeature ownedPackagesFeat = p.eClass().getEStructuralFeature("ownedPackages");
			EStructuralFeature ownedPackagesFeatD = packageClass.getEStructuralFeature("ownedPackages");
			List<Package> ownedPackages = (List<Package>) p.eGet(ownedPackagesFeat);
			List<EObject> ownedPackagesD = transfoPackage(ownedPackages);
			packageObject.eSet(ownedPackagesFeatD, ownedPackagesD);

			EStructuralFeature ownedElementsFeat = p.eClass().getEStructuralFeature("ownedElements");
			EStructuralFeature ownedElementsFeatD = packageClass.getEStructuralFeature("ownedElements");
			List<BodyDeclaration> ownedElements = (List<BodyDeclaration>) p.eGet(ownedElementsFeat);
			List<EObject> ownedElementsD = transfoClassDeclaration(ownedElements);
			packageObject.eSet(ownedElementsFeatD, ownedElementsD);

			getProxyAndComment(p, packageObject);

			packagesD.add(packageObject);
		}
		return packagesD;
	}

	private static void getProxyAndComment(ASTNode node, EObject packageObject) {
		EStructuralFeature commentsFeat = node.eClass().getEStructuralFeature("comments");
		EStructuralFeature commentsFeatD = packageObject.eClass().getEStructuralFeature("comments");
		List<Comment> comments = (List<Comment>) node.eGet(commentsFeat);
		List<EObject> commentsD = transfoComments(comments);
		packageObject.eSet(commentsFeatD, commentsD);

		EStructuralFeature proxyFeat = node.eClass().getEStructuralFeature("proxy");
		EStructuralFeature proxyFeatD = packageObject.eClass().getEStructuralFeature("proxy");
		Boolean proxy = (Boolean) node.eGet(proxyFeat);
		packageObject.eSet(proxyFeatD, proxy);
	}

	private static List<EObject> transfoClassDeclaration(List<BodyDeclaration> bodyDeclarations) {
		List<EObject> classesD = new ArrayList<>();
		for (BodyDeclaration p : bodyDeclarations) {
			if (p instanceof ClassDeclaration) {
				EObject classDeclarationObject = dataPackage.getEFactoryInstance().create(classDeclarationClass);
				classDeclarationObject.eSet(classDeclarationClass.getEStructuralFeature("name"), p.getName());
				getProxyAndComment(p, classDeclarationObject);

				EStructuralFeature declarationsFeat = p.eClass().getEStructuralFeature("bodyDeclarations");
				EStructuralFeature declarationsFeatD = classDeclarationClass.getEStructuralFeature("bodyDeclarations");
				List<BodyDeclaration> declarations = (List<BodyDeclaration>) p.eGet(declarationsFeat);
				List<EObject> declarationsD = transfoBodyDeclarations(declarations);
				classDeclarationObject.eSet(declarationsFeatD, declarationsD);

				classesD.add(classDeclarationObject);
			}

		}
		return classesD;
	}

	private static List<EObject> transfoComments(List<Comment> comments) {
		List<EObject> commentsD = new ArrayList<>();
		for (Comment c : comments) {
			EObject commentObject = dataPackage.getEFactoryInstance().create(commentsClass);

			EStructuralFeature contentFeat = c.eClass().getEStructuralFeature("content");
			EStructuralFeature contentFeatD = commentsClass.getEStructuralFeature("content");
			String content = (String) c.eGet(contentFeat);
			commentObject.eSet(contentFeatD, content);
			
			commentsD.add(commentObject);

		}
		return commentsD;
	}

	private static List<EObject> transfoBodyDeclarations(List<BodyDeclaration> declarations) {
		List<EObject> bodyDeclarationsD = new ArrayList<>();
		for (BodyDeclaration p : declarations) {
			if (p instanceof FieldDeclaration) {
				EObject fieldObject = dataPackage.getEFactoryInstance().create(fieldDeclarationClass);
				String name = ((FieldDeclaration) p).getFragments().get(0).getName();
				fieldObject.eSet(fieldDeclarationClass.getEStructuralFeature("name"), name);
				
				String type = "String";
				
				if (((FieldDeclaration) p).getType() != null) {
					type = ((FieldDeclaration) p).getType().getType().getName();
				}
				fieldObject.eSet(fieldDeclarationClass.getEStructuralFeature("type"), type);
				
				getProxyAndComment(p, fieldObject);
				bodyDeclarationsD.add(fieldObject);

			}
			if (p instanceof MethodDeclaration) {
				EObject methodObject = dataPackage.getEFactoryInstance().create(methodDeclerationClass);
				methodObject.eSet(methodDeclerationClass.getEStructuralFeature("name"), p.getName());
				getProxyAndComment(p, methodObject);
				bodyDeclarationsD.add(methodObject);

			} else if (p instanceof ConstructorDeclaration) {
				EObject constructorObject = dataPackage.getEFactoryInstance().create(constructorDeclarationClass);
				constructorObject.eSet(constructorDeclarationClass.getEStructuralFeature("name"), p.getName());
				getProxyAndComment(p, constructorObject);
				bodyDeclarationsD.add(constructorObject);

			}

		}
		return bodyDeclarationsD;
	}

}
