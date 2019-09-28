package com.rj.ardemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

//    ArSceneView arSceneView;
    boolean modelIsLoaded = false;
    boolean isSolarSystemPlaced = false;
    ModelRenderable sunRender;
    ModelRenderable earthRender;
    ModelRenderable jupiterRender;
    ModelRenderable lunaRender;
    ModelRenderable marsRender;
    ModelRenderable mercuryRender;
    ModelRenderable neptuneRender;
    ModelRenderable saturnRender;
    ModelRenderable uranusRender;
    ModelRenderable venusRender;
    ViewRenderable solarSystemControlsRender;
    FloatingActionButton record;
//    boolean isRecording=false;
    VideoRecorder videoRecorder;


    SolarSettings solarSettings = new SolarSettings();
    //    GestureDetector gestureDetector;
    private static final float AU_TO_METERS = 0.5f;
    private static final String TAG = "MainActivity";
    //    boolean installRequested;
//    Snackbar loadingMessageSnackbar = null;
    MediaPlayer mediaPlayer;
    ArFragment arFragment;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        setContentView(R.layout.activity_main);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.weightless);
        mediaPlayer.start();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFrag);
        if (arFragment != null) {
            arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
                onSingleTap(motionEvent);
                //            Anchor anchor = hitResult.createAnchor();
                //            ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build()
                //                    .thenAccept(modelRenderable -> addModel(anchor,modelRenderable))
                //            .exceptionally(throwable -> {
                //                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //                builder.setMessage("Something Went wrong");
                //                builder.show();
                //                return null;
                //            });

            }));
        }
//        arSceneView = findViewById(R.id.ar_scene_view);
        record = findViewById(R.id.buttonRecord);

        CompletableFuture<ModelRenderable> sunStage = ModelRenderable.builder().setSource(this, Uri.parse("Sol.sfb")).build();
        CompletableFuture<ModelRenderable> earthStage = ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build();
        CompletableFuture<ModelRenderable> jupiterStage = ModelRenderable.builder().setSource(this, Uri.parse("Jupiter.sfb")).build();
        CompletableFuture<ModelRenderable> lunaStage = ModelRenderable.builder().setSource(this, Uri.parse("Luna.sfb")).build();
        CompletableFuture<ModelRenderable> marsStage = ModelRenderable.builder().setSource(this, Uri.parse("Mars.sfb")).build();
        CompletableFuture<ModelRenderable> mercuryStage = ModelRenderable.builder().setSource(this, Uri.parse("Mercury.sfb")).build();
        CompletableFuture<ModelRenderable> neptuneStage = ModelRenderable.builder().setSource(this, Uri.parse("Neptune.sfb")).build();
        CompletableFuture<ModelRenderable> saturnStage = ModelRenderable.builder().setSource(this, Uri.parse("Saturn.sfb")).build();
        CompletableFuture<ModelRenderable> uranusStage = ModelRenderable.builder().setSource(this, Uri.parse("Uranus.sfb")).build();
        CompletableFuture<ModelRenderable> venusStage = ModelRenderable.builder().setSource(this, Uri.parse("Venus.sfb")).build();
        CompletableFuture<ViewRenderable> solarSystemControlStage = ViewRenderable.builder().setView(this, R.layout.solar_controls).build();
        CompletableFuture.allOf(
                sunStage, earthStage, jupiterStage, lunaStage, marsStage, mercuryStage, neptuneStage, saturnStage, uranusStage, venusStage, solarSystemControlStage
        ).handle((x, t) -> {
            if (t != null) {
                Utils.displayError(this, "Cannot Render", t);
            }
            try {
                sunRender = sunStage.get();
                earthRender = earthStage.get();
                jupiterRender = jupiterStage.get();
                lunaRender = lunaStage.get();
                marsRender = marsStage.get();
                mercuryRender = mercuryStage.get();
                neptuneRender = neptuneStage.get();
                saturnRender = saturnStage.get();
                uranusRender = uranusStage.get();
                venusRender = venusStage.get();
                solarSystemControlsRender = solarSystemControlStage.get();
                modelIsLoaded = true;
            } catch (InterruptedException | ExecutionException e) {
                Utils.displayError(this, "Cannot Load Renderables", e);
                e.printStackTrace();
            }
            return null;
        });

//        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                Log.d(TAG, "onSingleTapUp: ");
//                Toast.makeText(MainActivity.this, "In gesture", Toast.LENGTH_SHORT).show();
//                onSingleTap(e);
//                return super.onSingleTapUp(e);
//            }
//        });
//        arSceneView.getScene().addOnUpdateListener(frameTime -> {
//            if (loadingMessageSnackbar == null) {
//                return;
//            }
//            Frame frame = arSceneView.getArFrame();
//            if (frame == null) {
//                return;
//            }
//            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
//                return;
//            }
//            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
//                if (plane.getTrackingState() == TrackingState.TRACKING) {
//                    hideLoadingMessage();
//                }
//            }
//        });

        record.setOnClickListener(this::toggleRecording);
        videoRecorder = new VideoRecorder();
        int orientation = getResources().getConfiguration().orientation;
        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation);
        videoRecorder.setSceneView(arFragment.getArSceneView());
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void toggleRecording(View view) {
        boolean recording = videoRecorder.onToggleRecord();
        if (recording) {
            record.setImageResource(R.drawable.ic_stop_black_24dp);
        } else {
            record.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
            String videoPath = videoRecorder.getVideoPath().getAbsolutePath();
            Toast.makeText(this, "Video saved: " + videoPath, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + videoPath);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.DATA, videoPath);
            getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    private void onSingleTap(MotionEvent e) {
        Toast.makeText(this, "in on single tap", Toast.LENGTH_SHORT).show();
        if (!modelIsLoaded) {
            return;
        }
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame != null) {
            if (!isSolarSystemPlaced && placeSolarSystem(e, frame)) {
                isSolarSystemPlaced = true;
            }
        }




    }


    private boolean placeSolarSystem(MotionEvent e, Frame frame) {
        if (e != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hitResult : frame.hitTest(e)) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                    Node solarSystem = createSolarSystem();
                    anchorNode.addChild(solarSystem);
                    return true;
                }
            }
        }
        return true;
    }

    private Node createSolarSystem() {
        Node base = new Node();
        Node sun = new Node();
        sun.setParent(base);
        sun.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));

        Node sunVisual = new Node();
        sunVisual.setParent(sun);
        sunVisual.setRenderable(sunRender);
        sunVisual.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

        Node solarControls = new Node();
        solarControls.setParent(sun);
        solarControls.setRenderable(solarSystemControlsRender);
        solarControls.setLocalPosition(new Vector3(0.0f, 0.50f, 0.0f));
        View solarControlsView = solarSystemControlsRender.getView();
        SeekBar orbitSpeedBar = solarControlsView.findViewById(R.id.orbitSpeedBar);
        orbitSpeedBar.setProgress((int) (solarSettings.getOrbitSpeedMultiplier() * 10.0f));
        orbitSpeedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float ratio = (float) progress / (float) orbitSpeedBar.getMax();
                        solarSettings.setOrbitSpeedMultiplier(ratio * 10.0f);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        SeekBar rotationSpeedBar = solarControlsView.findViewById(R.id.rotationSpeedBar);
        rotationSpeedBar.setProgress((int) (solarSettings.getRotationSpeedMultiplier() * 10.0f));
        rotationSpeedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float ratio = (float) progress / (float) rotationSpeedBar.getMax();
                        solarSettings.setRotationSpeedMultiplier(ratio * 10.0f);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        // Toggle the solar controls on and off by tapping the sun.
        sunVisual.setOnTapListener(
                (hitTestResult, motionEvent) -> solarControls.setEnabled(!solarControls.isEnabled()));
        createPlanet("Mercury", sun, 0.5f, 47f, mercuryRender, 0.019f, 0.03f);
        createPlanet("Venus", sun, 0.7f, 35f, venusRender, 0.0475f, 2.64f);
        Node earth = createPlanet("Earth", sun, 1.0f, 29f, earthRender, 0.05f, 23.4f);
        createPlanet("Moon", earth, 0.15f, 100f, lunaRender, 0.018f, 6.68f);
        createPlanet("Mars", sun, 1.5f, 24f, marsRender, 0.0265f, 25.19f);
        createPlanet("Jupiter", sun, 2.2f, 13f, jupiterRender, 0.16f, 3.13f);
        createPlanet("Saturn", sun, 3.5f, 9f, saturnRender, 0.1325f, 26.73f);
        createPlanet("Uranus", sun, 5.2f, 7f, uranusRender, 0.1f, 82.23f);
        createPlanet("Neptune", sun, 6.1f, 5f, neptuneRender, 0.074f, 28.32f);

        return base;
    }

    private Node createPlanet(
            String name,
            Node parent,
            float auFromParent,
            float orbitDegreesPerSecond,
            ModelRenderable renderable,
            float planetScale,
            float axisTilt) {
        // Orbit is a rotating node with no renderable positioned at the sun.
        // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
        // This is done instead of making the sun rotate so each planet can orbit at its own speed.
        RotatingNode orbit = new RotatingNode(solarSettings, true, false, 0);
        orbit.setDegreesPerSecond(orbitDegreesPerSecond);
        orbit.setParent(parent);

        // Create the planet and position it relative to the sun.
        Planet planet =
                new Planet(
                        this, name, planetScale, orbitDegreesPerSecond, axisTilt, renderable, solarSettings);
        planet.setParent(orbit);
        planet.setLocalPosition(new Vector3(auFromParent * AU_TO_METERS, 0.0f, 0.0f));
        return planet;
    }


//    private void addModel(Anchor anchor, ModelRenderable modelRenderable) {
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
//        transformableNode.setParent(anchorNode);
//        transformableNode.setRenderable(modelRenderable);
//        arFragment.getArSceneView().getScene().addChild(anchorNode);
//        transformableNode.select();
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (arSceneView == null) {
//            return;
//        }
//        if (arSceneView.getSession() == null) {
//            // If the session wasn't created yet, don't resume rendering.
//            // This can happen if ARCore needs to be updated or permissions are not granted yet.
//            try {
//                Session session = Utils.createArSession(this, installRequested);
//                if (session == null) {
//                    installRequested = Utils.hasCameraPermission(this);
//                    return;
//                } else {
//                    arSceneView.setupSession(session);
//                }
//            } catch (UnavailableException e) {
//                Utils.handleSessionException(this, e);
//            }
//        }
//        try {
//            arSceneView.resume();
//        } catch (CameraNotAvailableException ex) {
//            Utils.displayError(this, "Unable to get camera", ex);
//            finish();
//            return;
//        }
//
//        if (arSceneView.getSession() != null) {
//            showLoadingMessage();
//        }


    }

//    private void showLoadingMessage() {
//        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
//            return;
//        }
//
//        loadingMessageSnackbar =
//                Snackbar.make(
//                        MainActivity.this.findViewById(android.R.id.content),
//                        "Searching for surfaces",
//                        Snackbar.LENGTH_INDEFINITE);
//        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
//        loadingMessageSnackbar.show();
//    }
//
//    private void hideLoadingMessage() {
//        if (loadingMessageSnackbar == null) {
//            return;
//        }
//
//        loadingMessageSnackbar.dismiss();
//        loadingMessageSnackbar = null;
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
//        if (arSceneView != null) {
//            arSceneView.pause();
//        }
        mediaPlayer.pause();
        if (videoRecorder.isRecording()) {
            toggleRecording(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!Utils.hasCameraPermission(this)) {
            if (!Utils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                Utils.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }

        if(requestCode == 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Video Recording Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Video Recording Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Audio Recording Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Audio Recording Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
