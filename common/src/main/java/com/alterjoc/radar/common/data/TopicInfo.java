package com.alterjoc.radar.common.data;

import org.jboss.capedwarf.common.data.Mode;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.common.serialization.JSONUtils;
import org.jboss.capedwarf.common.sql.Column;
import org.jboss.capedwarf.common.sql.OnLoad;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Simple topic info.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@OnLoad("com.alterjoc.radar.client.database.sqlite.TopicLoadImageListener")
public class TopicInfo extends Timestamped
{
    private String name;
    private String description;
    private Type type;
    private Mode mode;

    private transient byte[] photo; // server side image
    private ImageInfo image; // client side image

    // client side only
    // if true, dont show the topic on map and list
    private boolean filterApplied;
    private boolean userSubscribed;
    private boolean userSubscribedOnSrv;
    private Long imagePk;

    public TopicInfo() {
    }

    public TopicInfo(String name) {
        this.name = name;
    }

    @Override
    public void writeJSONObject(JSONObject json) throws JSONException {
        super.writeJSONObject(json);
        json.put("name", name);
        json.putOpt("desc", description);
        JSONUtils.writeEnum(json, "type", type);
        JSONUtils.writeEnum(json, "mode", mode);
        JSONUtils.writeArray(json, "photo", photo);
    }

    public void readJSONObject(JSONObject json) throws JSONException {
        super.readJSONObject(json);
        name = json.getString("name");
        description = json.optString("desc");
        type = JSONUtils.readEnum(json, "type", null, Type.class);
        mode = JSONUtils.readEnum(json, "mode", null, Mode.class);
        photo = JSONUtils.readArray(json, "photo");
    }

    public ImageInfo getImage() {
        return image;
    }

    public void setImage(ImageInfo image) {
        this.image = image;
    }

    @Size(max = 1000000)
    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    @NotNull
    @Size(min = 5, max = 50)
    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Size(max = 50)
    @Column
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Column
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Column
    public boolean isUserSubscribed() {
        return userSubscribed;
    }

    public void setUserSubscribed(boolean userSubscribed) {
        this.userSubscribed = userSubscribed;
    }

    @Column(defaultValue = "0")
    public boolean isFilterApplied() {
        return filterApplied;
    }

    public void setFilterApplied(boolean filterApplied) {
        this.filterApplied = filterApplied;
    }

    public void update(TopicInfo other) {
        name = other.name;
        description = other.description;
    }

    public void initUserSubscribed() {
        if (type == Type.SYSTEM || type == Type.DEFAULT)
            userSubscribed = true;
    }

    @Column
    public boolean isUserSubscribedOnSrv() {
        return userSubscribedOnSrv;
    }

    public void setUserSubscribedOnSrv(boolean value) {
        userSubscribedOnSrv = value;
    }

    @Column
    public Long getImagePk() {
        return imagePk;
    }

    public void setImagePk(Long imagePk) {
        this.imagePk = imagePk;
    }

    public String toShortString() {
        return "TopicInfo [id: " + getId() + ", name: " + name + "]";
    }
}
