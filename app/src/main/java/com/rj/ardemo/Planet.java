package com.rj.ardemo;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Planet extends Node implements Node.OnTapListener {
    private final String planetName;
    private final float planetScale;
    private final float orbitDegreesPerSecond;
    private final float axisTilt;
    private final ModelRenderable planetRenderable;
    private final SolarSettings solarSettings;

    private Node infoCard;
    private RotatingNode planetVisual;
    private final Context context;

    private static final float INFO_CARD_Y_POS_COEFF = 0.55f;

    public Planet(
            Context context,
            String planetName,
            float planetScale,
            float orbitDegreesPerSecond,
            float axisTilt,
            ModelRenderable planetRenderable,
            SolarSettings solarSettings) {
        this.context = context;
        this.planetName = planetName;
        this.planetScale = planetScale;
        this.orbitDegreesPerSecond = orbitDegreesPerSecond;
        this.axisTilt = axisTilt;
        this.planetRenderable = planetRenderable;
        this.solarSettings = solarSettings;
        setOnTapListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void onActivate() {

        if (getScene() == null) {
            throw new IllegalStateException("Scene is null!");
        }

        if (infoCard == null) {
            infoCard = new Node();
            if(planetName.isEmpty()){
                Toast.makeText(context, "No names found", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onActivate: No names found");
            }
            infoCard.setParent(this);
            infoCard.setEnabled(false);
            infoCard.setLocalPosition(new Vector3(0.0f, planetScale * INFO_CARD_Y_POS_COEFF, 0.0f));

            ViewRenderable.builder()
                    .setView(context, R.layout.planet)
                    .build()
                    .thenAccept(
                            (renderable) -> {
                                infoCard.setRenderable(renderable);
                                TextView textView = renderable.getView().findViewById(R.id.planetInfoCard);
                                textView.setText(planetName);
                            })
                    .exceptionally(
                            (throwable) -> {
                                Log.d(TAG, "onActivate: Cannot Render Planet Content");
                                throw new AssertionError("Could not load plane card view.", throwable);
                            });
        }

        if (planetVisual == null) {
            // Put a rotator to counter the effects of orbit, and allow the planet orientation to remain
            // of planets like Uranus (which has high tilt) to keep tilted towards the same direction
            // wherever it is in its orbit.
            RotatingNode counterOrbit = new RotatingNode(solarSettings, true, true, 0f);
            counterOrbit.setDegreesPerSecond(orbitDegreesPerSecond);
            counterOrbit.setParent(this);

            planetVisual = new RotatingNode(solarSettings, false, false, axisTilt);
            planetVisual.setParent(counterOrbit);
            planetVisual.setRenderable(planetRenderable);
            planetVisual.setLocalScale(new Vector3(planetScale, planetScale, planetScale));
        }
    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        if (infoCard == null) {
            return;
        }

        // Typically, getScene() will never return null because onUpdate() is only called when the node
        // is in the scene.
        // However, if onUpdate is called explicitly or if the node is removed from the scene on a
        // different thread during onUpdate, then getScene may be null.
        if (getScene() == null) {
            return;
        }
        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = infoCard.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        infoCard.setWorldRotation(lookRotation);
    }

    @Override
    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if (infoCard == null) {
            return;
        }
        infoCard.setEnabled(!infoCard.isEnabled());
    }
}