package net.jmecn.mabi;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

import net.jmecn.mabi.struct.AniFile;
import net.jmecn.mabi.struct.AniFrame;
import net.jmecn.mabi.struct.AniTrack;
import net.jmecn.mabi.struct.BoneAssignment;
import net.jmecn.mabi.struct.FrmBone;
import net.jmecn.mabi.struct.FrmFile;
import net.jmecn.mabi.struct.PmGeometry;
import net.jmecn.mabi.struct.PmgFile;
import net.jmecn.mabi.struct.Skin;

/**
 * 用于加载和显示Mabinogi的模型文件
 * 
 * @author yanmaoyuan
 *
 */
public class ModelState extends BaseAppState {

	static Logger logger = LoggerFactory.getLogger(ModelState.class);
	
	private FrmFile frmFile;
	private PmgFile pmgFile;
	private List<AniFile> aniFiles;

	private Node rootNode;

	// 模型
	private Node model;
	private List<Node> addons;

	// 骨骼
	private Skeleton ske;
	private SkeletonControl skeletonControl;
	private SkeletonDebugger sd;
	private Material sdMat;
	
	// 动画
	private List<Animation> anims;
	private AnimControl animControl;
	private AnimChannel channel;

	private AssetManager assetManager;

	public ModelState() {
		rootNode = new Node("model");
		rootNode.setLocalScale(0.05f);

		model = new Node();
		rootNode.attachChild(model);

		addons = new ArrayList<Node>();
		aniFiles = new ArrayList<AniFile>();
		anims = new ArrayList<Animation>();
	}

	@Override
	protected void initialize(Application app) {
		assetManager = app.getAssetManager();

		// 骨骼的材质
		sdMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		sdMat.setColor("Color", ColorRGBA.Red);
		sdMat.getAdditionalRenderState().setDepthTest(false);
		
//		loadSkeleton("pet/mesh/fox/pet_c4_maplestory_fox01_s_framework.frm");
		loadModel("pet/mesh/fox/pet_c4_maplestory_fox01_s_mesh.pmg");
		//loadAnimation("pet/anim/maplefox/pet_maplefox_s_attack_01.ani");
		//loadAnimation("pet/anim/maplefox/pet_maplefox_s_natural_sit_01.ani");
		loadAnimation("pet/anim/maplefox/pet_maplefox_s_stand_friendly.ani");
	}

	@Override
	protected void cleanup(Application app) {
	}

	@Override
	protected void onEnable() {
		SimpleApplication app = (SimpleApplication) getApplication();
		app.getRootNode().attachChild(rootNode);
	}

	@Override
	protected void onDisable() {
		rootNode.removeFromParent();
	}

	/**
	 * 装载骨骼
	 * @param frm
	 */
	public void loadSkeleton(String frm) {
		
		if (frmFile != null) {
			frmFile = null;
			
			ske = null;
		}
		
		// 骨架
		frmFile = (FrmFile) assetManager.loadAsset(frm);
		aniFiles.clear();
		
		ske = buildSkeleton(frmFile);
		resetSkeletonDebugger(ske);
		resetAnimControl(ske);
	}
	
	/**
	 * 根据frm文件，生成骨骼。
	 * @param frmFile
	 * @return
	 */
	private Skeleton buildSkeleton(FrmFile frmFile) {
		int boneCount = frmFile.boneCount;
		
		Bone[] bones = new Bone[boneCount];
		byte[] parents = new byte[boneCount];
		for (int i = 0; i < boneCount; i++) {
			FrmBone fbone = frmFile.frmBones[i];
			
			String name = fbone.getSimpleName();
			Bone bone = new Bone(name);

			Matrix4f bindPose = fbone.bindPose;
			bone.setBindTransforms(bindPose.toTranslationVector(), bindPose.toRotationQuat(), bindPose.toScaleVector());
			
			// 父子关系
			bones[fbone.boneid] = bone;
			parents[fbone.boneid] = fbone.parentid;
		}
		
		// 继承关系
		for(byte id=0; id<boneCount; id++) {
			byte parentid = parents[id];
			if (parentid > -1 && parentid < boneCount && parentid != id) {
				bones[parentid].addChild(bones[id]);
			}
		}
				
		Skeleton ske = new Skeleton(bones);
		return ske;
	}

	/**
	 * Skeleton Debug
	 * @param ske
	 */
	private void resetSkeletonDebugger(Skeleton ske) {
		if (sd != null) {
			sd.removeFromParent();
			sd = null;
		}
		
		sd = new SkeletonDebugger("SkeletonDebugger", ske);
		sd.setMaterial(sdMat);
		
		rootNode.attachChild(sd);
	}
	
	/**
	 * 重置动画数据
	 * @param ske
	 */
	private void resetAnimControl(Skeleton ske) {
		if (animControl != null) {
			rootNode.removeControl(animControl);
			rootNode.removeControl(skeletonControl);
			anims.clear();
			channel = null;
		}

		animControl = new AnimControl(ske);
		skeletonControl = new SkeletonControl(ske);
		channel = animControl.createChannel();
		
		rootNode.addControl(animControl);
		rootNode.addControl(skeletonControl);
	}
	
	/**
	 * 装载pmg模型
	 * @param path
	 */
	public void loadModel(String path) {
		if (pmgFile != null) {
			// TODO clear the models
		}
		
		pmgFile = (PmgFile) assetManager.loadAsset(path);
		buildModel(pmgFile, model);
	}

	/**
	 * 加载额外的pmg模型
	 * @param path
	 */
	public void addModel(String path) {
		PmgFile pmgFile = (PmgFile) assetManager.loadAsset(path);
		Node model = buildModel(pmgFile, null);
		addons.add(model);
		rootNode.attachChild(model);
	}

	/**
	 * 生成模型。
	 * @param pmgFile
	 * @param model
	 * @return
	 */
	private Node buildModel(PmgFile pmgFile, Node model) {
		if (model != null) {
			model.detachAllChildren();
		} else {
			model = new Node();
		}
		
		int count = pmgFile.geomCount;
		for (int k = 0; k < count; k++) {
			PmGeometry pmg = pmgFile.geomDats[k];
			
			Mesh mesh = buildMesh(pmg);
			
			// 骨骼蒙皮数据
			if (ske != null && pmg.skinCount > 0) {
				BoneAssignment boneAssignment = pmgFile.boneAssignments[k];
				skinning(mesh, pmg, boneAssignment);
			}

			mesh.setStatic();
			mesh.updateCounts();
			mesh.updateBound();
			
			Geometry geom = new Geometry(pmg.meshName, mesh);
			geom.setLocalRotation(pmg.majorMatrix.toRotationQuat());
			geom.setLocalTranslation(pmg.majorMatrix.toTranslationVector());
			geom.setLocalScale(pmg.majorMatrix.toScaleVector());

			// 创建材质
			Material mat = buildMaterial(pmg);
			geom.setMaterial(mat);
			
			model.attachChild(geom);
		}
		
		return model;
	}
	
	/**
	 * 创建网格
	 * @param pmg
	 * @return
	 */
	private Mesh buildMesh(PmGeometry pmg) {
		Mesh mesh = new Mesh();

		// 顶点数据
		int[] indexes = pmg.indexes;
		Vector3f[] vertexes = new Vector3f[pmg.vertexCount];
		Vector3f[] normals = new Vector3f[pmg.vertexCount];
		ColorRGBA[] vertexColors = new ColorRGBA[pmg.vertexCount];
		Vector2f[] texCoords = new Vector2f[pmg.vertexCount];
		for (int i = 0; i < pmg.vertexCount; i++) {
			vertexes[i] = pmg.verts[i].getPosition();
			normals[i] = pmg.verts[i].getNormal();
			vertexColors[i] = pmg.verts[i].getColor();
			texCoords[i] = pmg.verts[i].getTexCoord();
		}
		// normals = MeshUtil.computeNormals(vertexes, indexes);
		mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indexes));
		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertexes));
		mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
		mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(vertexColors));
		mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));

		return mesh;
	}
	
	/**
	 * 骨骼蒙皮
	 * @param mesh
	 * @param pmg
	 * @param boneAssignment
	 */
	private void skinning(Mesh mesh, PmGeometry pmg, BoneAssignment boneAssignment) {
		mesh.setMaxNumWeights(2);
		int ba = ske.getBoneIndex(boneAssignment.bone1);
		int bb = ske.getBoneIndex(boneAssignment.bone2);

		byte[] boneIndexes = new byte[pmg.vertexCount * 4];
		float[] boneWeights = new float[pmg.vertexCount * 4];
		for(int i = 0; i<pmg.vertexCount; i++) {
			int n = i * 4;
			boneIndexes[n] = (byte) ba;
			boneIndexes[n + 1] = (byte) bb;
			boneIndexes[n + 2] = 0;
			boneIndexes[n + 3] = 0;
			
			// default weight
			boneWeights[n] = 0.5f;
			boneWeights[n + 1] = 0.5f;
			boneWeights[n + 2] = 0;
			boneWeights[n + 3] = 0;
		}
		
		for (int i = 0; i < pmg.skinCount; i++) {
			
			Skin skin = pmg.skins[i];
			
			float a = skin.boneWeight;
			float b = (1 - skin.boneWeight);

			int n = skin.id * 4;
			boneWeights[n] = a;
			boneWeights[n + 1] = b;
			boneWeights[n + 2] = 0;
			boneWeights[n + 3] = 0;
		}

		FloatBuffer vertex = (FloatBuffer)mesh.getBuffer(Type.Position).getData();
		FloatBuffer normals = (FloatBuffer)mesh.getBuffer(Type.Normal).getData();
		
		mesh.setBuffer(Type.BindPosePosition, 3, vertex);
		mesh.setBuffer(Type.BindPoseNormal, 3, normals);
		
		mesh.setBuffer(Type.BoneIndex, 4, BufferUtils.createByteBuffer(boneIndexes));
		mesh.setBuffer(Type.HWBoneIndex, 4, BufferUtils.createByteBuffer(boneIndexes));
		
		mesh.setBuffer(Type.BoneWeight, 4, BufferUtils.createFloatBuffer(boneWeights));
		mesh.setBuffer(Type.HWBoneWeight, 4, BufferUtils.createFloatBuffer(boneWeights));
	}
	
	/**
	 * 创建材质
	 * @param pmg
	 * @return
	 */
	private Material buildMaterial(PmGeometry pmg) {
		Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		mat.setBoolean("UseVertexColor", true);

		mat.setBoolean("UseMaterialColors", true);
		mat.setColor("Ambient", new ColorRGBA(1f, 1f, 1f, 1f));
		mat.setColor("Diffuse", new ColorRGBA(0, 0, 0, 1));
		mat.setColor("Specular", new ColorRGBA(0, 0, 0, 1));

		try {
			TextureKey key = new TextureKey(pmg.textureName + ".dds", false);
			Texture tex = assetManager.loadTexture(key);
			mat.setTexture("DiffuseMap", tex);
		} catch (Exception e) {
			logger.error("can't find:{}", pmg.textureName);
		}

		mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		
		return mat;
	}
	
	/**
	 * 载入动画
	 * @param path
	 */
	public void loadAnimation(String path) {
		AniFile aniFile = (AniFile) assetManager.loadAsset(path);
		// TODO 先检查动画和骨骼是否匹配，然后再创建动画。
		
		aniFiles.add(aniFile);
		
		Animation anim = buildAnimation(aniFile);
		
		anims.add(anim);
		playAnim(anim);
	}
	
	/**
	 * 生成动画
	 * @param aniFile
	 * @return
	 */
	private Animation buildAnimation(AniFile aniFile) {
		// 动画名称
        String name = aniFile.getName();
        // 动画时长
        float length = aniFile.getLength();
        
		Animation anim = new Animation(name, length);
		
		for (int i = 0; i < aniFile.boneCount; i++) {
			AniTrack aniTrack = aniFile.aniTracks[i];
			
			BoneTrack track = new BoneTrack(i);
			anim.addTrack(track);

			float[] times = new float[aniTrack.frameCount];
			Vector3f[] translations = new Vector3f[aniTrack.frameCount];
			Quaternion[] rotations = new Quaternion[aniTrack.frameCount];

			for (int j = 0; j < aniTrack.frameCount; j++) {
				AniFrame aniFrame = aniTrack.aniFrames[j];
				times[j] = (float) aniFrame.frameNo / aniFile.framePerSecond;
				translations[j] = new Vector3f(aniFrame.x, aniFrame.y, aniFrame.z);
				rotations[j] = new Quaternion(-aniFrame.qx, -aniFrame.qy, -aniFrame.qz, aniFrame.qw);
				
				if (j == 0) {
					// TODO 设定骨骼的初始pose
				}
			}

			track.setKeyframes(times, translations, rotations);
		}
		
		return anim;
	}
	
	/**
	 * 播放动画
	 * @param anim
	 */
	private void playAnim(Animation anim) {
		if (ske != null) {
			animControl.addAnim(anim);

			clearBonePose();
			channel.setAnim(anim.getName());
		} else {
			logger.warn("no skeleton");
		}
	}

	/**
	 * 可能是jme3引擎的问题，播放动画时，第一帧是不能有数值的。。
	 * 
	 * @param ske
	 */
	private void clearBonePose() {
		for (int i = 0; i < ske.getBoneCount(); i++) {
			Bone bone = ske.getBone(i);
			bone.setBindTransforms(new Vector3f(), new Quaternion(), null);
		}
	}

	public void removeSkeleton() {

	}
}
