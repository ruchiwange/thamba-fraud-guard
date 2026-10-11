"""App guard: is an installed app the kind fraudsters ask people to install?

Two patterns are checked:
  1. An app that did not come from an app store and can read SMS. This is how
     a "challan" or "invitation" file steals one-time codes.
  2. A remote-access app. These are genuine tools, but a caller who asks for
     one to be installed can then watch the screen.
"""

# Package names are from memory and should be re-checked against the Play Store.
REMOTE_ACCESS = {
    "com.anydesk.anydeskandroid": "AnyDesk",
    "com.teamviewer.quicksupport.market": "TeamViewer QuickSupport",
    "com.teamviewer.teamviewer.market.mobile": "TeamViewer",
    "com.carriez.flutter_hbb": "RustDesk",
}

APP_STORES = {
    "com.android.vending",              # Google Play
    "com.sec.android.app.samsungapps",  # Samsung Galaxy Store
    "com.xiaomi.mipicks",               # Xiaomi GetApps
    "com.heytap.market",                # Oppo and Realme
    "com.oppo.market",
    "com.vivo.appstore",
    "com.huawei.appmarket",
    "com.amazon.venezia",
}

_SMS_PERMISSIONS = {"android.permission.RECEIVE_SMS", "android.permission.READ_SMS"}


def check_app(package, installer, permissions, is_system=False):
    """Return a dict: level, reason, name.

    package      the app's package name
    installer    package name of whatever installed it, or "" if unknown
    permissions  the permissions the app requests
    is_system    True for apps that came with the phone
    """
    if package in REMOTE_ACCESS:
        return {"level": "amber", "reason": "app_remote_access", "name": REMOTE_ACCESS[package]}
    if is_system:
        return {"level": "none", "reason": None, "name": None}

    from_store = (installer or "") in APP_STORES
    reads_sms = bool(_SMS_PERMISSIONS & set(permissions or []))
    if not from_store and reads_sms:
        return {"level": "red", "reason": "app_sideloaded_reads_sms", "name": None}
    return {"level": "none", "reason": None, "name": None}
