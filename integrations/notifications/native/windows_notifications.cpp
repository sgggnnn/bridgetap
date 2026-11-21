#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>
#include <roapi.h>
#include <DispatcherQueue.h>
#include <jni.h>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Foundation.Collections.h>
#include <winrt/Windows.System.h>
#include <winrt/Windows.ApplicationModel.h>
#include <winrt/Windows.UI.Notifications.h>
#include <winrt/Windows.UI.Notifications.Management.h>
#include <atomic>
#include <chrono>
#include <deque>
#include <mutex>
#include <set>
#include <string>
#include <vector>

using namespace winrt;
using namespace winrt::Windows::UI::Notifications;
using namespace winrt::Windows::UI::Notifications::Management;
using winrt::Windows::System::DispatcherQueueController;
namespace {
struct Notice { std::wstring id, appId, app, title, message, created; };
std::mutex guard;
std::deque<Notice> pending;
std::wstring status=L"off";
std::atomic<unsigned long long> generation{0};
DispatcherQueueController controller{nullptr};

void set_status(unsigned long long epoch,std::wstring value) {
    std::scoped_lock lock(guard);if(epoch==generation.load())status=std::move(value);
}
void initialize_com() {
    static thread_local bool initialized=false;if(initialized)return;
    HRESULT hr=RoInitialize(RO_INIT_MULTITHREADED);
    if(FAILED(hr)&&hr!=RPC_E_CHANGED_MODE)check_hresult(hr);
    initialized=true;
}
std::wstring code(hresult value) {wchar_t text[40];swprintf_s(text,L"unavailable:0x%08X",static_cast<unsigned>(value.value));return text;}
long long millis(winrt::Windows::Foundation::DateTime time) {
    return std::chrono::duration_cast<std::chrono::milliseconds>(time.time_since_epoch()).count()-11644473600000LL;
}
std::wstring key(UserNotification const& value) {return std::to_wstring(value.Id())+L":"+std::to_wstring(millis(value.CreationTime()));}
std::wstring bounded(hstring const& value,size_t limit) {return std::wstring(value.c_str(),std::min<size_t>(value.size(),limit));}

fire_and_forget listen(unsigned long long epoch,bool request) {
    try {
        auto dispatcher=winrt::Windows::System::DispatcherQueue::GetForCurrentThread();
        auto listener=UserNotificationListener::Current();
        auto access=request?co_await listener.RequestAccessAsync():listener.GetAccessStatus();
        if(epoch!=generation.load())co_return;
        if(access!=UserNotificationListenerAccessStatus::Allowed) {set_status(epoch,L"denied");co_return;}
        auto existing=co_await listener.GetNotificationsAsync(NotificationKinds::Toast);
        if(epoch!=generation.load())co_return;
        std::set<std::wstring> seen;
        for(auto const& value:existing)seen.insert(key(value));
        set_status(epoch,L"allowed");
        while(epoch==generation.load()) {
            co_await resume_after(std::chrono::milliseconds(750));
            co_await resume_foreground(dispatcher);
            if(epoch!=generation.load())co_return;
            if(listener.GetAccessStatus()!=UserNotificationListenerAccessStatus::Allowed){set_status(epoch,L"denied");co_return;}
            auto values=co_await listener.GetNotificationsAsync(NotificationKinds::Toast);
            if(epoch!=generation.load())co_return;
            if(seen.size()>4096){seen.clear();for(auto const& value:values)seen.insert(key(value));continue;}
            for(auto const& value:values) {
                try {
                    auto id=key(value);if(!seen.insert(id).second)continue;
                    auto binding=value.Notification().Visual().GetBinding(KnownNotificationBindings::ToastGeneric());
                    if(!binding)continue;
                    auto texts=binding.GetTextElements();
                    auto info=value.AppInfo();Notice event;
                    event.id=id;event.appId=bounded(info.AppUserModelId(),240);event.app=bounded(info.DisplayInfo().DisplayName(),64);
                    event.created=std::to_wstring(millis(value.CreationTime()));
                    bool first=true;
                    for(auto const& text:texts) {
                        if(first){event.title=bounded(text.Text(),120);first=false;}
                        else if(event.message.size()<400){if(!event.message.empty())event.message+=L" ";event.message+=bounded(text.Text(),400-event.message.size());}
                    }
                    std::scoped_lock lock(guard);if(epoch!=generation.load())co_return;
                    while(pending.size()>=128)pending.pop_front();pending.push_back(std::move(event));
                }catch(hresult_error const&){/* A broken toast must not stop other applications. */}
            }
        }
    }catch(hresult_error const& error){set_status(epoch,code(error.code()));}
     catch(...){set_status(epoch,L"unavailable");}
}
jstring java_string(JNIEnv* env,std::wstring const& value) {return env->NewString(reinterpret_cast<jchar const*>(value.data()),static_cast<jsize>(value.size()));}
}

extern "C" JNIEXPORT void JNICALL Java_com_bridgetapclient_notifications_WindowsNotifications_nStart(JNIEnv*,jclass,jboolean request) {
    auto epoch=++generation;
    try {
        initialize_com();
        if(!controller) {
            DispatcherQueueOptions options{sizeof(DispatcherQueueOptions),DQTYPE_THREAD_DEDICATED,DQTAT_COM_STA};
            check_hresult(CreateDispatcherQueueController(options,reinterpret_cast<PDISPATCHERQUEUECONTROLLER*>(put_abi(controller))));
        }
        set_status(epoch,request?L"requesting":L"starting");
        if(!controller.DispatcherQueue().TryEnqueue([epoch,request]{listen(epoch,request!=JNI_FALSE);}))set_status(epoch,L"unavailable");
    }catch(hresult_error const& error){set_status(epoch,code(error.code()));}
     catch(...){set_status(epoch,L"unavailable");}
}
extern "C" JNIEXPORT void JNICALL Java_com_bridgetapclient_notifications_WindowsNotifications_nStop(JNIEnv*,jclass) {
    ++generation;std::scoped_lock lock(guard);pending.clear();status=L"off";
}
extern "C" JNIEXPORT jstring JNICALL Java_com_bridgetapclient_notifications_WindowsNotifications_nStatus(JNIEnv* env,jclass) {
    std::scoped_lock lock(guard);return java_string(env,status);
}
extern "C" JNIEXPORT jstring JNICALL Java_com_bridgetapclient_notifications_WindowsNotifications_nProbe(JNIEnv* env,jclass) {
    try {initialize_com();auto access=UserNotificationListener::Current().GetAccessStatus();return java_string(env,access==UserNotificationListenerAccessStatus::Allowed?L"allowed":L"denied");}
    catch(hresult_error const& error){return java_string(env,code(error.code()));}catch(...){return java_string(env,L"unavailable");}
}
extern "C" JNIEXPORT jobjectArray JNICALL Java_com_bridgetapclient_notifications_WindowsNotifications_nDrain(JNIEnv* env,jclass) {
    std::deque<Notice> ready;{std::scoped_lock lock(guard);ready.swap(pending);}
    auto stringClass=env->FindClass("java/lang/String");auto result=env->NewObjectArray(static_cast<jsize>(ready.size()*6),stringClass,nullptr);
    if(!result)return nullptr;jsize index=0;
    for(auto const& value:ready)for(auto text:{&value.id,&value.appId,&value.app,&value.title,&value.message,&value.created}) {
        auto item=java_string(env,*text);if(!item)return result;env->SetObjectArrayElement(result,index++,item);env->DeleteLocalRef(item);
    }
    return result;
}
// Synthetic transport check: does not request access or read Windows notifications.
extern "C" JNIEXPORT void JNICALL Java_com_bridgetapclient_notifications_WindowsNotifications_nSynthetic(JNIEnv*,jclass) {
    std::scoped_lock lock(guard);pending.push_back({L"jni-test",L"test.app",L"Native test",L"Unicode \u00e9 \u4f60\u597d",L"Synthetic notification",L"0"});
}
