# Android M 权限请求类库
本类库中包含一个在Android M及以下版本中实现权限请求的Activity基类

## 用法示例
自定义Activity继承MActivity，
在需要获取文件读写权限时，执行如下方法

<pre>
boolean hasPermission = requirePermissions(1, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
android.Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionEvent(){
    @Override
    void permissionGranted() {
        doOperation();
    }
    @Override
    void permissionDenied(String[] permissions) {
        Toast.makeText(this,"PermissionDenied",Toast.LENGTH_SHORT).show();
    }
});
if(hasPermission) {
    doOperation();
}
</pre>