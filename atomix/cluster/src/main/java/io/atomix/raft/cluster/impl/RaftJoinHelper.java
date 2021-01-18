package io.atomix.raft.cluster.impl;

import io.atomix.cluster.MemberId;
import io.atomix.raft.RaftError;
import io.atomix.raft.cluster.RaftMember;
import io.atomix.raft.protocol.JoinRequest;
import io.atomix.raft.protocol.JoinResponse;
import io.atomix.raft.protocol.RaftResponse;
import io.atomix.utils.concurrent.Scheduled;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaftJoinHelper {

  private static final Logger LOG = LoggerFactory.getLogger(RaftJoinHelper.class);
  private final BiFunction<MemberId, JoinRequest, CompletableFuture<JoinResponse>> joinRequestSender;
  private final BiFunction<Duration, Runnable, Scheduled> scheduler;
  private final Consumer<JoinResponse> joinResponseConsumer;
  private final Duration delayedJoinDuration;
  private Scheduled joinTimeout;
  private final Executor executor;

  public RaftJoinHelper(
      final Executor executor,
      final BiFunction<MemberId, JoinRequest, CompletableFuture<JoinResponse>> joinRequestSender,
      final BiFunction<Duration, Runnable, Scheduled> scheduler,
      final Duration delayedJoinDuration,
      final Consumer<JoinResponse> joinResponseConsumer) {
    this.executor = executor;
    this.joinRequestSender = joinRequestSender;
    this.scheduler = scheduler;
    this.delayedJoinDuration = delayedJoinDuration;
    this.joinResponseConsumer = joinResponseConsumer;
  }

  public void join(final Iterator<RaftMemberContext> iterator,
      final Supplier<RaftMember> raftMemberSupplier,
      final Supplier<Iterator<RaftMemberContext>> newIteratorSupplier) {
    if (iterator.hasNext()) {
      cancelJoinTimer();
      joinTimeout = scheduler.apply(delayedJoinDuration,
          () -> join(iterator, raftMemberSupplier, newIteratorSupplier));

      final RaftMemberContext member = iterator.next();

      LOG.debug("Attempting to join via {}", member.getMember().memberId());

      final JoinRequest request =
          JoinRequest.builder()
              .withMember(raftMemberSupplier.get())
              .build();
      joinRequestSender.apply(member.getMember().memberId(), request)
          .whenCompleteAsync(
              (response, error) -> {
                // Cancel the join timer.
                cancelJoinTimer();

                if (error == null) {
                  if (response.status() == RaftResponse.Status.OK) {
                    LOG.debug("Successfully joined via {}", member.getMember().memberId());

//                    final Configuration configuration =
//                        new Configuration(
//                            response.index(),
//                            response.term(),
//                            response.timestamp(),
//                            response.members());
//
//                    // Configure the cluster with the join response.
//                    // Commit the configuration as we know it was committed via the successful join
//                    // response.
//                    configure(configuration).commit();

                    joinResponseConsumer.accept(response);
//                    completeJoinFuture();
                  } else if (response.error() == null
                      || response.error().type() == RaftError.Type.CONFIGURATION_ERROR) {
                    // If the response error is null, that indicates that no error occurred but the
                    // leader was
                    // in a state that was incapable of handling the join request. Attempt to join
                    // the leader
                    // again after an election timeout.
                    LOG.debug(
                        "Failed to join {}, probably leader but currently not able to accept the join request. Retry later.",
                        member.getMember().memberId());
                    join(newIteratorSupplier.get(), raftMemberSupplier, newIteratorSupplier);
                  } else {
                    // If the response error was non-null, attempt to join via the next server in
                    // the members list.
                    LOG.debug(
                        "Failed to join {}. Received error {}",
                        member.getMember().memberId(),
                        response.error());

                    join(iterator, raftMemberSupplier, newIteratorSupplier);
                  }
                } else {
                  LOG.debug("Failed to join {}", member.getMember().memberId(), error);
                  join(iterator, raftMemberSupplier, newIteratorSupplier);
                }
              }, executor);
    }
    // If join attempts remain, schedule another attempt after two election timeouts. This allows
    // enough time
    // for servers to potentially timeout and elect a leader.
    else {
      LOG.debug("Failed to join cluster, retrying...");
      join(newIteratorSupplier.get(), raftMemberSupplier, newIteratorSupplier);
    }
  }

  /**
   * Cancels the join timeout.
   */
  private void cancelJoinTimer() {
    if (joinTimeout != null) {
      LOG.trace("Cancelling join timeout");
      joinTimeout.cancel();
      joinTimeout = null;
    }
  }
}
