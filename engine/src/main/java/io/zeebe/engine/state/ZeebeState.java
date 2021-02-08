/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.state;

import io.zeebe.engine.state.mutable.MutableBlackListState;
import io.zeebe.engine.state.mutable.MutableDeploymentState;
import io.zeebe.engine.state.mutable.MutableElementInstanceState;
import io.zeebe.engine.state.mutable.MutableEventScopeInstanceState;
import io.zeebe.engine.state.mutable.MutableIncidentState;
import io.zeebe.engine.state.mutable.MutableJobState;
import io.zeebe.engine.state.mutable.MutableMessageStartEventSubscriptionState;
import io.zeebe.engine.state.mutable.MutableMessageState;
import io.zeebe.engine.state.mutable.MutableMessageSubscriptionState;
import io.zeebe.engine.state.mutable.MutableTimerInstanceState;
import io.zeebe.engine.state.mutable.MutableVariableState;
import io.zeebe.engine.state.mutable.MutableWorkflowInstanceSubscriptionState;
import io.zeebe.engine.state.mutable.MutableWorkflowState;

public interface ZeebeState {

  MutableDeploymentState getDeploymentState();

  MutableWorkflowState getWorkflowState();

  MutableJobState getJobState();

  MutableMessageState getMessageState();

  MutableMessageSubscriptionState getMessageSubscriptionState();

  MutableMessageStartEventSubscriptionState getMessageStartEventSubscriptionState();

  MutableWorkflowInstanceSubscriptionState getWorkflowInstanceSubscriptionState();

  MutableIncidentState getIncidentState();

  KeyGenerator getKeyGenerator();

  MutableBlackListState getBlackListState();

  MutableVariableState getVariableState();

  MutableTimerInstanceState getTimerState();

  MutableElementInstanceState getElementInstanceState();

  MutableEventScopeInstanceState getEventScopeInstanceState();

  int getPartitionId();

  boolean isEmpty(final ZbColumnFamilies column);
}
