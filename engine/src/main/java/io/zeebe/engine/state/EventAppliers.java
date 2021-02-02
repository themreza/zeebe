/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.state;

import io.zeebe.engine.state.appliers.WorkflowInstanceElementActivatedApplier;
import io.zeebe.msgpack.UnpackedObject;
import io.zeebe.protocol.impl.record.RecordMetadata;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.Intent;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import java.util.Map;

/**
 * Applies state changes from events to the {@link io.zeebe.engine.state.ZeebeState}.
 *
 * <p>Finds the correct {@link EventApplier} and delegates.
 */
public final class EventAppliers {

  private static final EventApplier UNIMPLEMENTED_EVENT_APPLIER =
      (key, intent, value, metadata) -> {};

  private final Map<ValueType, Map<Intent, EventApplier>> mapping;

  public EventAppliers(final ZeebeState state) {
    mapping =
        Map.of(
            ValueType.WORKFLOW_INSTANCE,
            Map.of(
                WorkflowInstanceIntent.ELEMENT_ACTIVATED,
                new WorkflowInstanceElementActivatedApplier(state)));
  }

  public void applyState(
      final long key,
      final Intent intent,
      final UnpackedObject value,
      final RecordMetadata metadata) {
    mapping
        .getOrDefault(metadata.getValueType(), Map.of())
        .getOrDefault(intent, UNIMPLEMENTED_EVENT_APPLIER)
        .applyState(key, intent, value, metadata);
  }
}
