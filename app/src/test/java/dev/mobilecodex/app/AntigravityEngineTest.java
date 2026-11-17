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
    @After public void close() throws Exception {
        engine.io.shutdownNow();
        Field timer = Engine.class.getDeclaredField("approvalTimer"); timer.setAccessible(true);
        ((ScheduledExecutorService)timer.get(engine)).shutdownNow();
    }
    @Test public void sendsAndResumesWithoutCodexLoginOrRpc() throws Exception {
        call("chat.send", obj("text", "hello", "model", "agy-model"));
        assertNotNull(request);
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
}
