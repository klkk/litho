/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.model;

import java.util.ArrayList;
import java.util.List;

import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.internal.ImmutableList;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Class for validating that Diff parameter usages within a {@link SpecModel} are well-formed.
 */
public class DiffValidation {

  static final String MISSING_TYPE_PARAMETER_ERROR =
      "Diff<T> parameters take exactly one type parameter T.";
  static final String PROP_MISMATCH_ERROR =
      "Diff<T> parameters annotated with @Prop must share the name of an " +
          "existing @Prop-annotated prop with the same type T.";
  static final String STATE_MISMATCH_ERROR =
      "Diff<T> parameters annotated with @State must share the name of an " +
          "existing @State-annotated prop with the same type T.";
  static final String MISSING_ANNOTATION_ERROR =
      "Diff<T> parameters must be annotated with either @Prop/@State and " +
          "share the name of an existing @Prop/@State param with the same type T.";

  static List<SpecModelValidationError> validate(SpecModel specModel) {
    final List<SpecModelValidationError> validationErrors = new ArrayList<>();
    final ImmutableList<DiffModel> diffModels = specModel.getDiffs();

    for (int i = 0, size = diffModels.size(); i < size; i++) {
      final DiffModel diffModel  = diffModels.get(i);

      final ParameterizedTypeName typeName =
          (diffModel.getType() instanceof ParameterizedTypeName) ?
              ((ParameterizedTypeName) diffModel.getType()) : null;
      if (typeName == null || typeName.typeArguments.size() != 1) {
        validationErrors.add(
            new SpecModelValidationError(
                diffModel.getRepresentedObject(),
                MISSING_TYPE_PARAMETER_ERROR));
        continue;
      }

      final TypeName expectedType = typeName.typeArguments.get(0).box();
      if (MethodParamModelUtils.isAnnotatedWith(diffModel, Prop.class)) {
        final PropModel propModel =
            SpecModelUtils.getPropWithName(specModel, diffModel.getName());
        if (propModel == null || !expectedType.equals(propModel.getType().box())) {
          validationErrors.add(
              new SpecModelValidationError(
                  diffModel.getRepresentedObject(),
                  PROP_MISMATCH_ERROR));
        }
      } else if (MethodParamModelUtils.isAnnotatedWith(diffModel, State.class)) {
        final StateParamModel stateValue =
            SpecModelUtils.getStateValueWithName(specModel, diffModel.getName());
        if (stateValue == null || !expectedType.equals(stateValue.getType().box())) {
          validationErrors.add(
              new SpecModelValidationError(
                  diffModel.getRepresentedObject(),
                  STATE_MISMATCH_ERROR));
        }
      } else {
        validationErrors.add(
            new SpecModelValidationError(
                diffModel.getRepresentedObject(),
                MISSING_ANNOTATION_ERROR));
      }
    }

    return validationErrors;
  }
}
