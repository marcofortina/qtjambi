/****************************************************************************
**
** Copyright (C) 1992-2009 Nokia. All rights reserved.
** Copyright (C) 2009-2020 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
**
** This file is part of Qt Jambi.
**
** ** $BEGIN_LICENSE$
**
** GNU Lesser General Public License Usage
** This file may be used under the terms of the GNU Lesser
** General Public License version 2.1 as published by the Free Software
** Foundation and appearing in the file LICENSE.LGPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU Lesser General Public License version 2.1 requirements
** will be met: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html.
**
** In addition, as a special exception, Nokia gives you certain
** additional rights. These rights are described in the Nokia Qt LGPL
** Exception version 1.0, included in the file LGPL_EXCEPTION.txt in this
** package.
**
** GNU General Public License Usage
** Alternatively, this file may be used under the terms of the GNU
** General Public License version 3.0 as published by the Free Software
** Foundation and appearing in the file LICENSE.GPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU General Public License version 3.0 requirements will be
** met: http://www.gnu.org/copyleft/gpl.html.
**
** $END_LICENSE$
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/

#include "cppgenerator.h"
#include "reporthandler.h"

#include "metajava.h"
#include "typesystem/typedatabase.h"

// ### There's a bug in Qt causing it to fail at normalizing signatures
// on the form FooBar<T> const&, which is the form the C++ parser uses
// for all types, so connections between Java and C++ with const& templates
// will fail. This is a work around which is only needed until that bug is fixed.
// Since Qt works correctly with const FooBar<T> &, we simply change the
// signature to that.
QString CppGenerator::fixNormalizedSignatureForQt(const QString &signature) {
    QString ret = signature;
    if (signature.contains("<") && signature.endsWith("const&")) {
        ret = "const "
              + signature.mid(0, signature.size() - 6)
              + "&";
    }
    return ret;
}

QString CppGenerator::subDirectoryForPackage(const QString &package) {
    TypeSystemTypeEntry * typeSystemEntry = static_cast<TypeSystemTypeEntry *>(TypeDatabase::instance()->findType(package));
    if(typeSystemEntry && !typeSystemEntry->qtLibrary().isEmpty()){
        if(typeSystemEntry->qtLibrary().startsWith("Qt") && !typeSystemEntry->qtLibrary().startsWith("QtJambi")){
            QString libName = typeSystemEntry->qtLibrary();
            return "QtJambi" + libName.mid(2);
        }else{
            return typeSystemEntry->qtLibrary();
        }
    }else
        return QString(package).replace(".", "_");
}

void CppGenerator::writeTypeInfo(QTextStream &s, const AbstractMetaType *type, Option options) {
    if ((options & OriginalTypeDescription) && !type->originalTypeDescription().isEmpty()) {
        QString originalTypeDescription = type->originalTypeDescription();

        if (options & NormalizeAndFixTypeSignature) {
            originalTypeDescription = QMetaObject::normalizedSignature(originalTypeDescription.toLatin1().constData());
            originalTypeDescription = fixNormalizedSignatureForQt(originalTypeDescription);
        }
        int mtype;
        if (originalTypeDescription.contains("qtjambireal")){ // map generator type to qreal type
            s << originalTypeDescription.replace("qtjambireal", "qreal");
            return;
        }else if (originalTypeDescription.contains("qreal")){ // map generator type to qreal type
            s << originalTypeDescription;
            return;
        }else{
            mtype = QMetaType::type(originalTypeDescription.toLatin1().constData());
        }
#if QT_VERSION < 0x050000
        if(mtype>QMetaType::Void && mtype<=QMetaType::LastCoreExtType){
#else
        if(mtype>QMetaType::UnknownType && mtype<=QMetaType::HighestInternalId){
#endif
            originalTypeDescription = QMetaType::typeName(mtype);
        }
        s << originalTypeDescription;
        return;
    }

    if (type->isArray()) {
        writeTypeInfo(s, type->arrayElementType(), Option(options & ~ForceConstReference));
        if (options & ArrayAsPointer) {
            s << "*";
        } else if (!(options & SkipArray)) {
            s << "[" << type->arrayElementCount() << "]";
        }
        return;
    }

    const TypeEntry *te = type->typeEntry();

    if ( (type->isConstant()
            && !(options & ExcludeConst))
         || ((options & ForceConstReference) == ForceConstReference
               && type->getReferenceType()==AbstractMetaType::NoReference
               && type->indirections().isEmpty()
               && !type->isPrimitive()
               && !type->isEnum()
               && !type->isFlags())){
        s << "const ";
    }
    if(type->isVolatile()){
        s << "volatile ";
    }

    if ((options & EnumAsInts) && (te->isEnum() || te->isFlags()) && type->indirections().isEmpty()) {
        uint size = 32;
        if(te->isEnum()){
            size = static_cast<const EnumTypeEntry*>(te)->size();
        }
        s << "qint" << size;
    } else if (te->isFlags()) {
        s << static_cast<const FlagsTypeEntry *>(te)->originalName();
    } else {
        s << fixCppTypeName(te->qualifiedCppName());
    }

    if (type->instantiations().size() > 0
            && (!type->isContainer()
            || (
                (static_cast<const ContainerTypeEntry *>(te))->type() != ContainerTypeEntry::StringListContainer
            &&  (static_cast<const ContainerTypeEntry *>(te))->type() != ContainerTypeEntry::ByteArrayListContainer
            &&  (static_cast<const ContainerTypeEntry *>(te))->type() != ContainerTypeEntry::QVector2DArrayContainer
            &&  (static_cast<const ContainerTypeEntry *>(te))->type() != ContainerTypeEntry::QVector3DArrayContainer
            &&  (static_cast<const ContainerTypeEntry *>(te))->type() != ContainerTypeEntry::QVector4DArrayContainer
               )
            )
        ) {
        s << '<';
        const QList<const AbstractMetaType *>& args = type->instantiations();
        bool nested_template = false;
        for (int i = 0; i < args.size(); ++i) {
            if (i != 0)
                s << ", ";
            nested_template |= args.at(i)->isContainer();
            writeTypeInfo(s, args.at(i), SkipName);
        }
        if (nested_template)
            s << ' ';
        s << '>';
    }

    for(int i=0; i<type->indirections().size(); i++){
        if(type->indirections()[i]){
            s << "*const ";
        }else{
            s << '*';
        }
    }

    if (type->getReferenceType()==AbstractMetaType::Reference && !(options & ExcludeReference)){
        s << "&";
    }else if (type->getReferenceType()==AbstractMetaType::RReference && !(options & ExcludeReference)){
        s << "&&";
    }else if((options & ForceConstReference) == ForceConstReference
             && type->indirections().isEmpty()
             && !type->isPrimitive()
             && !type->isEnum()
             && !type->isFlags()){
        s << "&";
    }

    if (!(options & SkipName))
        s << ' ';
}


void CppGenerator::writeFunctionArguments(QTextStream &s,
        const AbstractMetaArgumentList &arguments,
        Option option,
        int numArguments) {
    if (numArguments < 0) numArguments = arguments.size();

    bool needComma = false;
    for (int i = 0; i < numArguments; ++i) {
        if (needComma)
            s << ", ";
        else
            needComma = true;
        AbstractMetaArgument *arg = arguments.at(i);
        if(arg->type()->isArray() && !(option & SkipName)){
            option = Option(option | SkipArray);
        }
        if(arg->type()->originalTypeDescription().startsWith("const ::")
                || arg->type()->originalTypeDescription().startsWith("::")){
            writeTypeInfo(s, arg->type(), Option(option | OriginalTypeDescription));
        }else{
            writeTypeInfo(s, arg->type(), option);
        }
        if (!(option & SkipName)){
            s << arg->indexedName();
            if(arg->type()->isArray()){
                s << "[" << arg->type()->arrayElementCount() << "]";
            }
        }
        if ((option & IncludeDefaultExpression) && !arg->originalDefaultValueExpression().isEmpty()) {
            s << " = ";

            QString expr = arg->originalDefaultValueExpression();
            if (arg->type()->typeEntry()->isEnum() && expr.indexOf("::") < 0)
                s << static_cast<const EnumTypeEntry *>(arg->type()->typeEntry())->qualifier() << "::";
            if (arg->type()->typeEntry()->isFlags() && expr.indexOf("::") < 0)
                s << static_cast<const FlagsTypeEntry *>(arg->type()->typeEntry())->originator()->qualifier() << "::";
            s << expr;
        }
    }
}

/*!
 * Writes the function \a java_function signature to the textstream \a s.
 *
 * The \a name_prefix can be used to give the function name a prefix,
 * like "__public_" or "__override_" and \a classname_prefix can
 * be used to give the class name a prefix.
 *
 * The \a option flags can be used to tweak various parameters, such as
 * showing static, original vs renamed name, underscores for space etc.
 *
 * The \a extra_arguments list is a list of extra arguments on the
 * form "bool static_call".
 */

void CppGenerator::writeFunctionSignature(QTextStream &s,
        const AbstractMetaFunction *java_function,
        const AbstractMetaClass *implementor,
        const QString &name_prefix,
        Option option,
        const QString &classname_prefix,
        const QStringList &extra_arguments,
        int numArguments) {
// ### remove the implementor
    AbstractMetaType *function_type = java_function->type();

    if(java_function->hasTemplateArgumentTypes()){
        s << "template<";
        for(AbstractMetaTemplateParameter * templateParameter : java_function->templateParameters()){
            if(templateParameter->type()){
                writeTypeInfo(s, templateParameter->type());
                s << templateParameter->name();
            }
        }
        s << ">";
    }

    if (java_function->isStatic() && (option & ShowStatic))
        s << "static ";

    if ((option & SkipReturnType) == 0) {
        if (function_type) {
            writeTypeInfo(s, function_type, Option(option | SkipName));
            s << " ";
        } else if (!java_function->isConstructor()) {
            s << "void ";
        }
    }

    if (implementor) {
        if (classname_prefix.isEmpty())
            s << shellClassName(implementor) << "::";
        else
            s << classname_prefix << implementor->qualifiedCppName() << "::";
    }


    QString function_name;
    if (option & OriginalName)
        function_name = java_function->originalName();
    else
        function_name = java_function->name();

    if (option & UnderscoreSpaces)
        function_name = function_name.replace(' ', '_');

    if (java_function->isConstructor())
        function_name = shellClassName(java_function->ownerClass());

    s << name_prefix << function_name;

    if (java_function->attributes() & AbstractMetaAttributes::SetterFunction){
        s << "_setter";
        option = Option(option | EnumAsInts);
    }else if (java_function->attributes() & AbstractMetaAttributes::GetterFunction){
        s << "_getter";
        option = Option(option | EnumAsInts);
    }

    s << "(";

    AbstractMetaArgumentList tmpList;
    if ((option & SkipRemovedArguments) == SkipRemovedArguments) {	// this should be a method
        const AbstractMetaArgumentList& arguments = java_function->arguments();
        for (int i = 0; i < arguments.size(); ++i) {
            if(java_function->argumentRemoved(i + 1)!=ArgumentRemove_No){
                if(!java_function->isConstructor()
                        || ( !arguments.at(i)->originalDefaultValueExpression().isEmpty()
                             && java_function->conversionRule(TypeSystem::NativeCode, i + 1).isEmpty()
                             && java_function->conversionRule(TypeSystem::TargetLangCode, i + 1).isEmpty() )){
                    continue;
                }
            }
            tmpList.append(arguments.at(i));
        }
    } else {
        tmpList.append(java_function->arguments());
    }
    writeFunctionArguments(s, tmpList, option, numArguments);

    // The extra arguments...
    bool needComma = tmpList.size() != 0 ? true : false;
    for (int i = 0; i < extra_arguments.size(); ++i) {
        if (needComma)
            s << ", ";
        else
            needComma = true;
        s << extra_arguments.at(i);
    }

    s << ")";
    if (java_function->isConstant())
        s << " const";
    if (!java_function->isConstructor() && !java_function->isFinalInCpp() && (option & ShowOverride))
        s << " override";
}

void getTypePPConditions(QStringList& ppConditions, const AbstractMetaType* type){
    if(type && !type->typeEntry()->isThread() && type->typeEntry()->qualifiedCppName()=="QThread"){
        if(!type->typeEntry()->ppCondition().isEmpty() && !ppConditions.contains(type->typeEntry()->ppCondition())){
            ppConditions << type->typeEntry()->ppCondition();
        }
        for(const AbstractMetaType* inst : type->instantiations()){
            getTypePPConditions(ppConditions, inst);
        }
    }
}

QStringList CppGenerator::getFunctionPPConditions(const AbstractMetaFunction *java_function){
    QStringList pps;
    for(const FunctionModification& mod : java_function->modifications(java_function->declaringClass())){
        if(!mod.ppCondition.isEmpty() && !pps.contains(mod.ppCondition)){
            pps << mod.ppCondition;
        }
    }
    getTypePPConditions(pps, java_function->type());
    for(const AbstractMetaArgument *argument : java_function->arguments()) {
        getTypePPConditions(pps, argument->type());
    }
    if(java_function->ownerClass() && !java_function->ownerClass()->typeEntry()->ppCondition().isEmpty()){
        pps.removeAll(java_function->ownerClass()->typeEntry()->ppCondition());
    }
    if(!pps.isEmpty()){
        for (int i=0; i<pps.size(); ++i) {
            if(pps[i].contains("|")){
                pps[i] = "(" + pps[i] + ")";
            }
        }
    }
    return pps;
}
