package io.atomix.raft.cluster.impl;

import io.atomix.cluster.MemberId;
import io.atomix.raft.RaftError;
import io.atomix.raft.cluster.RaftMember.Type;
import io.atomix.raft.protocol.JoinResponse;
import io.atomix.raft.protocol.RaftResponse;
import io.atomix.raft.protocol.RaftResponse.Status;
import io.atomix.utils.concurrent.SingleThreadContext;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class RaftJoinHelperTest {


  private SingleThreadContext singleThreadContext;

  @Before
  public void setup() {
    singleThreadContext = new SingleThreadContext("test");
  }

  @Test
  public void shouldJoin() throws Exception {
    // given
    final var raftMembers = List
        .of(createRaftMemberContext("1"), createRaftMemberContext("2"),
            createRaftMemberContext("3"));
    final CountDownLatch latch = new CountDownLatch(1);
    final var joinResponse = createSuccessfulJoinResponse();
    final var raftJoinHelper = new RaftJoinHelper(singleThreadContext,
        ((memberId, joinRequest) -> CompletableFuture.completedFuture(joinResponse)),
        singleThreadContext::schedule,
        Duration.ofMillis(10), (response) -> latch.countDown());

    // when
    singleThreadContext.execute(() -> {
      raftJoinHelper
          .join(raftMembers.iterator(), () -> createRaftMember("4"), () -> raftMembers.iterator());
    });

    // then
    latch.await(15, TimeUnit.SECONDS);

  }

  @Test
  public void shouldJoinOnlyOneNode() throws Exception {
    // given
    final var raftMembers = List
        .of(createRaftMemberContext("1"), createRaftMemberContext("2"),
            createRaftMemberContext("3"));
    final CountDownLatch latch = new CountDownLatch(1);
    final var joinResponse = createSuccessfulJoinResponse();
    final var noLeaderJoinResponse = createNoLeaderJoinResponse();
    final var raftJoinHelper = new RaftJoinHelper(singleThreadContext,
        ((memberId, joinRequest) -> {
          if (memberId.id().equals("2")) {
            return CompletableFuture.completedFuture(joinResponse);
          }

          return CompletableFuture.completedFuture(noLeaderJoinResponse);
        }),
        singleThreadContext::schedule,
        Duration.ofMillis(10), (response) -> latch.countDown());

    // when
    singleThreadContext.execute(() -> {
      raftJoinHelper
          .join(raftMembers.iterator(), () -> createRaftMember("4"), () -> raftMembers.iterator());
    });

    // then
    latch.await(15, TimeUnit.SECONDS);

  }

  private RaftMemberContext createRaftMemberContext(final String id) {
    return new RaftMemberContext(createRaftMember(id), null, 0);
  }

  private DefaultRaftMember createRaftMember(final String id) {
    return new DefaultRaftMember(MemberId.from(id), Type.ACTIVE, Instant.now());
  }

  private JoinResponse createSuccessfulJoinResponse() {
    return JoinResponse.builder()
        .withStatus(Status.OK)
        .withIndex(1)
        .withTerm(1)
        .withTime(System.currentTimeMillis())
        .withMembers(List.of()).build();
  }

  private JoinResponse createNoLeaderJoinResponse() {
    return JoinResponse.builder()
        .withStatus(RaftResponse.Status.ERROR)
        .withError(RaftError.Type.NO_LEADER).build();
  }
}
