package com.jetbrains.python.documentation;

import com.google.common.base.CharMatcher;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyDocStringOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class DocStringReferenceProvider extends PsiReferenceProvider {
  private final String[] ALL_PARAM_TAGS;

  public DocStringReferenceProvider() {
    List<String> allParamTags = new ArrayList<String>();
    for (String tag : EpydocString.PARAM_TAGS) {
      allParamTags.add("@" + tag);
      allParamTags.add(":" + tag);
    }
    ALL_PARAM_TAGS = ArrayUtil.toStringArray(allParamTags);
  }

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull ProcessingContext context) {
    final PyDocStringOwner docStringOwner = PsiTreeUtil.getParentOfType(element, PyDocStringOwner.class);
    if (docStringOwner != null && element == docStringOwner.getDocStringExpression()) {
      final List<PsiReference> result = new ArrayList<PsiReference>();
      String docString = element.getText();
      int pos = 0;
      while (pos < docString.length()) {
        final TextRange tagRange = findNextTag(docString, pos, ALL_PARAM_TAGS);
        if (tagRange == null) {
          break;
        }
        pos = CharMatcher.anyOf(" \t*").negate().indexIn(docString, tagRange.getEndOffset());
        int endPos = CharMatcher.JAVA_LETTER_OR_DIGIT.negate().indexIn(docString, pos);
        if (endPos < 0) {
          endPos = docString.length();
        }
        result.add(new DocStringParameterReference(element, new TextRange(pos, endPos)));
        pos = endPos;
      }

      return result.toArray(new PsiReference[result.size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  public static TextRange findNextTag(String docString, int pos, String[] paramTags) {
    int result = Integer.MAX_VALUE;
    String foundTag = null;
    for (String paramTag : paramTags) {
      int tagPos = docString.indexOf(paramTag, pos);
      while(tagPos >= 0 && tagPos + paramTag.length() < docString.length() &&
            Character.isLetterOrDigit(docString.charAt(tagPos + paramTag.length()))) {
        tagPos = docString.indexOf(paramTag, tagPos+1);
      }
      if (tagPos >= 0 && tagPos < result) {
        foundTag = paramTag;
        result = tagPos;
      }
    }
    return foundTag == null ? null : new TextRange(result, result + foundTag.length());
  }
}
