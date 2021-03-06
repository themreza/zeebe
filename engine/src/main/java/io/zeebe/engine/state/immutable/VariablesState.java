/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.state.immutable;

import io.zeebe.engine.state.instance.DbVariableState.VariableListener;
import java.util.Collection;
import org.agrona.DirectBuffer;

public interface VariablesState {

  DirectBuffer getVariableLocal(long scopeKey, DirectBuffer name);

  DirectBuffer getVariable(long scopeKey, DirectBuffer name);

  DirectBuffer getVariable(long scopeKey, DirectBuffer name, int nameOffset, int nameLength);

  DirectBuffer getVariablesAsDocument(long scopeKey);

  DirectBuffer getVariablesAsDocument(long scopeKey, Collection<DirectBuffer> names);

  DirectBuffer getVariablesLocalAsDocument(long scopeKey);

  DirectBuffer getTemporaryVariables(long scopeKey);

  boolean isEmpty();

  void setListener(VariableListener listener);
}
