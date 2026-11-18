package dev.mobilecodex.app;

import android.app.Application;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.io.File;
import java.lang.reflect.*;
import java.util.concurrent.*;
import static dev.mobilecodex.app.core.Json.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=Application.class)
public class AntigravityEngineTest {
    private Engine engine;
    private JSONObject request;
    private String runId;

    @Before public void setup() throws Exception {
        var context = RuntimeEnvironment.getApplication();
        new File(context.getFilesDir(), "sessions.json").delete();
        engine = new Engine(context);
        engine.setTestTransport((method, params) -> { throw new AssertionError("Codex RPC invoked: " + method); });
        engine.setTestAgentTransport((action, args, reply) -> {
            if (action.equals("agents.run")) { request = args; runId = args.optString("runId"); }
            reply.complete(obj("ok", true), null);
        });
    }
    private JSONObject call(String action, JSONObject args) throws Exception {
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        engine.handle(action, args, (value, error) -> { if (error == null) result.complete(value); else result.completeExceptionally(error); });
        return result.get(10, TimeUnit.SECONDS);
    }
    private void event(JSONObject data) throws Exception {
        Method method = Engine.class.getDeclaredMethod("agentRunEvent", JSONObject.class);
        method.setAccessible(true);
        engine.io.submit(() -> { method.invoke(engine, data); return null; }).get(10, TimeUnit.SECONDS);
    }
    private JSONObject attachment(String id) throws Exception {
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        Method method = Engine.class.getDeclaredMethod("roomHost", String.class, JSONObject.class, Engine.Reply.class);
        method.setAccessible(true);
        method.invoke(engine, "host.tool", obj("runId", runId, "roomId", request.optString("sessionId"),
            "name", "mobile_attachment", "arguments", obj("id", id)), (Engine.Reply)(value, error) -> {
                if (error == null) result.complete(value); else result.completeExceptionally(error);
            });
        return result.get(10, TimeUnit.SECONDS);
    }
    @After public void close() throws Exception {
        engine.io.shutdownNow();
        Field timer = Engine.class.getDeclaredField("approvalTimer"); timer.setAccessible(true);
        ((ScheduledExecutorService)timer.get(engine)).shutdownNow();
    }
    @Test public void sendsAndResumesWithoutCodexLoginOrRpc() throws Exception {
        call("chat.send", obj("text", "hello", "model", "agy-model", "chatIconsEnabled", true));
        assertNotNull(request);
        assertFalse(request.getString("prompt").contains("[[icon:"));
        assertEquals("agy", request.getString("provider"));
        assertEquals("agy-model", request.getString("model"));
        String local = call("state", obj()).getString("threadId");
        event(obj("runId", runId, "kind", "session", "id", "native-agy-session"));
        event(obj("runId", runId, "kind", "delta", "text", "Hello"));
        event(obj("runId", runId, "kind", "final", "text", "Hello", "sessionId", "native-agy-session"));
        assertFalse(call("state", obj()).getBoolean("busy"));
        call("chat.new", obj());
        call("chat.resume", obj("id", local));
        call("chat.send", obj("text", "continue", "model", "agy-model"));
        assertEquals("native-agy-session", request.getString("nativeSession"));
        call("chat.stop", obj());
        assertFalse(call("state", obj()).getBoolean("busy"));
    }
    @Test public void approvalChoiceFollowsOnlyItsConversation() throws Exception {
        JSONObject change = obj("agents", array(obj("id", "gemini", "approvalMode", "allow-all")), "expectedThreadId", "", "workspaceKey", "");
        call("chat.agents.set", change);
        call("chat.send", obj("text", "hello", "model", "agy-model"));
        assertEquals("allow-all", request.getString("approvalMode"));
        assertEquals("chat", request.getString("mode"));
        String thread = call("state", obj()).getString("threadId");
        call("chat.stop", obj());
        call("chat.new", obj());
        call("chat.send", obj("text", "new", "model", "agy-model"));
        assertEquals("ask", request.getString("approvalMode"));
        call("chat.stop", obj());
        call("chat.resume", obj("id", thread));
        assertThrows(ExecutionException.class, () -> call("chat.agents.set", change));
        call("chat.send", obj("text", "resume", "model", "agy-model"));
        assertEquals("allow-all", request.getString("approvalMode"));
    }
    @Test public void attachmentOnlyPromptReadsSharedFileAndRejectsUnsharedFile() throws Exception {
        var context = RuntimeEnvironment.getApplication();
        AttachmentStore store = new AttachmentStore(context, new ImageStore(context));
        JSONObject shared = store.store(new java.io.ByteArrayInputStream("shared note".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "note.txt", "text/plain");
        JSONObject other = store.store(new java.io.ByteArrayInputStream("private note".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "private.txt", "text/plain");
        call("instructions.save", obj("content", "Use concise Korean answers."));
        call("chat.send", obj("text", "", "model", "agy-model", "attachments", array(shared.getString("id"))));
        assertTrue(request.getString("prompt").contains(shared.getString("id")));
        assertTrue(request.getString("prompt").contains("Use concise Korean answers."));
        assertTrue(attachment(shared.getString("id")).toString().contains("shared note"));
        try { attachment(other.getString("id")); fail("Unshared attachment was readable"); }
        catch (ExecutionException expected) { assertTrue(expected.getCause().getMessage().contains("not shared")); }
        call("chat.stop", obj());
        try { attachment(shared.getString("id")); fail("Stopped turn retained attachment access"); }
        catch (ExecutionException expected) { assertNotNull(expected.getCause()); }
    }
}
