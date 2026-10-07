package megalodonte.script.fixtures;

import megalodonte.script.ViewScript;

public final class TestPage extends ViewScript {
    public static int submissions;
    public static int actions;
    public static int mounts;
    public static int destroys;
    @Override public void onMount() { mounts++; }
    @Override public void onDestroy() { destroys++; }
    @Override public void onSubmit() { submissions++; message("submitted " + text("name")); }
    public void doWork() { actions++; message("custom action"); }
}
