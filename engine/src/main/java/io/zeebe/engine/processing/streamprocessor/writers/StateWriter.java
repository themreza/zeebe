/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.processing.streamprocessor.writers;

import io.zeebe.engine.state.EventAppliers;
import io.zeebe.msgpack.UnpackedObject;
import io.zeebe.protocol.impl.record.RecordMetadata;
import io.zeebe.protocol.record.intent.Intent;
import java.util.function.Consumer;

/**
 * This event writer changes state for the events it writes to the stream.
 *
 * <p>Note that it does not write events to the stream itself, but it delegates this to the {@link
 * TypedStreamWriter}.
 *
 * <p>Note that it does not change the state itself, but delegates this to the {@link
 * EventAppliers}.
 */
public final class StateWriter implements TypedEventWriter {

  private static final Consumer<RecordMetadata> NO_METADATA = metadata -> {};

  private final TypedStreamWriter streamWriter;
  private final EventAppliers eventAppliers;

  public StateWriter(final TypedStreamWriter streamWriter, final EventAppliers eventAppliers) {
    this.streamWriter = streamWriter;
    this.eventAppliers = eventAppliers;
  }

  @Override
  public void appendNewEvent(final long key, final Intent intent, final UnpackedObject value) {
    appendFollowUpEvent(key, intent, value, NO_METADATA);
  }

  @Override
  public void appendFollowUpEvent(final long key, final Intent intent, final UnpackedObject value) {
    appendFollowUpEvent(key, intent, value, NO_METADATA);
  }

  @Override
  public void appendFollowUpEvent(
      final long key,
      final Intent intent,
      final UnpackedObject value,
      final Consumer<RecordMetadata> metadata) {
    // using the consumer as a callback, the event appliers have access to the metadata
    final Consumer<RecordMetadata> callback =
        data -> eventAppliers.applyState(key, intent, value, data);
    streamWriter.appendFollowUpEvent(key, intent, value, callback.andThen(metadata));
  }
}
