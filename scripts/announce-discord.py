#!/usr/bin/env python3
"""Announce a published release. The webhook is supplied only by Actions secrets."""
import json, os, sys, time, urllib.request, urllib.error
from pathlib import Path

def payload(release):
    tag=release.get('tag_name','')
    url=release.get('html_url','')
    if not url.startswith('https://github.com/sgggnnn/bridgetap/releases/tag/'):
        raise ValueError('Unexpected release URL')
    body=release.get('body','').strip() or 'See the release notes for additions and fixes.'
    if len(body)>3800: body=body[:3750]+'\n\n[Full changelog]('+url+')'
    return {'allowed_mentions':{'parse':[]},'embeds':[{'title':'BridgeTap '+tag+' is available','url':url,'description':body,'color':0x63CED0,'footer':{'text':'Minecraft 1.8.9 • Forge / OneClient'}}]}

def main():
    release=json.loads(Path(os.environ['GITHUB_EVENT_PATH']).read_text(encoding='utf-8'))['release']
    if release.get('draft'): raise ValueError('Draft announcements are disabled')
    data=json.dumps(payload(release)).encode()
    webhook=os.environ.get('DISCORD_UPDATE_WEBHOOK','')
    if not webhook.startswith('https://discord.com/api/webhooks/') or '?' in webhook:
        raise ValueError('Configure DISCORD_UPDATE_WEBHOOK in repository secrets')
    for attempt in range(3):
        try:
            req=urllib.request.Request(webhook,data=data,headers={'Content-Type':'application/json','User-Agent':'BridgeTap-release-announcements/1.0'},method='POST')
            with urllib.request.urlopen(req,timeout=20) as response:
                if response.status not in (200,204):raise RuntimeError('Discord rejected the announcement')
            print('Discord release announcement sent')
            return
        except urllib.error.HTTPError as exc:
            if exc.code==429 and attempt<2:
                try: delay=min(10,max(1,float(json.loads(exc.read(4096)).get('retry_after',2))))
                except Exception: delay=2
                time.sleep(delay)
                continue
            raise RuntimeError('Discord announcement failed (HTTP '+str(exc.code)+')') from None
        except urllib.error.URLError:
            raise RuntimeError('Discord announcement could not connect') from None

if __name__=='__main__':
    try:main()
    except Exception as error:
        print(str(error),file=sys.stderr)
        sys.exit(1)
