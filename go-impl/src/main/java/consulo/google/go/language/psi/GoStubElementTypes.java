package consulo.google.go.language.psi;

import com.goide.GoTypes;
import consulo.language.ast.IElementType;

/**
 * @author VISTALL
 * @since 07-Aug-22
 */
public interface GoStubElementTypes {
  IElementType ANONYMOUS_FIELD_DEFINITION = GoTypes.ANONYMOUS_FIELD_DEFINITION;
  IElementType ARRAY_OR_SLICE_TYPE = GoTypes.ARRAY_OR_SLICE_TYPE;
  IElementType CHANNEL_TYPE = GoTypes.CHANNEL_TYPE;
  IElementType CONST_DEFINITION = GoTypes.CONST_DEFINITION;
  IElementType CONST_SPEC = GoTypes.CONST_SPEC;
  IElementType FIELD_DEFINITION = GoTypes.FIELD_DEFINITION;
  IElementType FUNCTION_DECLARATION = GoTypes.FUNCTION_DECLARATION;
  IElementType FUNCTION_TYPE = GoTypes.FUNCTION_TYPE;
  IElementType IMPORT_SPEC = GoTypes.IMPORT_SPEC;
  IElementType INTERFACE_TYPE = GoTypes.INTERFACE_TYPE;
  IElementType LABEL_DEFINITION = GoTypes.LABEL_DEFINITION;
  IElementType MAP_TYPE = GoTypes.MAP_TYPE;
  IElementType METHOD_DECLARATION = GoTypes.METHOD_DECLARATION;
  IElementType METHOD_SPEC = GoTypes.METHOD_SPEC;
  IElementType PACKAGE_CLAUSE = GoTypes.PACKAGE_CLAUSE;
  IElementType PARAMETERS = GoTypes.PARAMETERS;
  IElementType PARAMETER_DECLARATION = GoTypes.PARAMETER_DECLARATION;
  IElementType PARAM_DEFINITION = GoTypes.PARAM_DEFINITION;
  IElementType PAR_TYPE = GoTypes.PAR_TYPE;
  IElementType POINTER_TYPE = GoTypes.POINTER_TYPE;
  IElementType RANGE_CLAUSE = GoTypes.RANGE_CLAUSE;
  IElementType RECEIVER = GoTypes.RECEIVER;
  IElementType RECV_STATEMENT = GoTypes.RECV_STATEMENT;
  IElementType RESULT = GoTypes.RESULT;
  IElementType SHORT_VAR_DECLARATION = GoTypes.SHORT_VAR_DECLARATION;
  IElementType SIGNATURE = GoTypes.SIGNATURE;
  IElementType SPEC_TYPE = GoTypes.SPEC_TYPE;
  IElementType STRUCT_TYPE = GoTypes.STRUCT_TYPE;
  IElementType TYPE = GoTypes.TYPE;
  IElementType TYPE_LIST = GoTypes.TYPE_LIST;
  IElementType TYPE_SPEC = GoTypes.TYPE_SPEC;
  IElementType VAR_DEFINITION = GoTypes.VAR_DEFINITION;
  IElementType VAR_SPEC = GoTypes.VAR_SPEC;
}