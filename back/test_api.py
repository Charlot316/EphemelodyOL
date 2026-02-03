import requests
import json

BASE_URL = "http://localhost:8090"

def test_get_chart(song_id):
    print(f"\n[TEST] /user/getChart?songId={song_id}")
    try:
        response = requests.get(f"{BASE_URL}/user/getChart", params={"songId": song_id})
        print(f"Status: {response.status_code}")
        data = response.json()
        if data['code'] == 0:
            print(f"SUCCESS: Song Name: {data['data'].get('songName')}")
        else:
            print(f"FAILED: {data['msg']}")
        return data
    except Exception as e:
        print(f"Error: {e}")
        return None

def test_get_all_charts():
    print(f"\n[TEST] /user/getAllCharts")
    try:
        response = requests.get(f"{BASE_URL}/user/getAllCharts")
        print(f"Status: {response.status_code}")
        data = response.json()
        if data['code'] == 0:
            charts = data['data'].get('charts', [])
            print(f"SUCCESS: Found {len(charts)} charts")
        else:
            print(f"FAILED: {data['msg']}")
        return data
    except Exception as e:
        print(f"Error: {e}")
        return None

def test_get_public_charts():
    print(f"\n[TEST] /user/getPublicCharts")
    try:
        # Testing with status=1 (published)
        payload = {"page": 1, "pageSize": 10, "status": 1}
        response = requests.post(f"{BASE_URL}/user/getPublicCharts", json=payload)
        print(f"Status: {response.status_code}")
        data = response.json()
        if data['code'] == 0:
            songs = data['data'].get('songs', [])
            print(f"SUCCESS: Found {len(songs)} public charts with status=1")
        else:
            print(f"FAILED: {data['msg']}")
        
        # Testing with status=2
        payload_s2 = {"page": 1, "pageSize": 10, "status": 2}
        response_s2 = requests.post(f"{BASE_URL}/user/getPublicCharts", json=payload_s2)
        data_s2 = response_s2.json()
        if data_s2['code'] == 0:
            songs_s2 = data_s2['data'].get('songs', [])
            print(f"SUCCESS: Found {len(songs_s2)} public charts with status=2")
        
        # Testing without status (should default to 1)
        print("Testing without status payload...")
        payload2 = {"page": 1, "pageSize": 10}
        response2 = requests.post(f"{BASE_URL}/user/getPublicCharts", json=payload2)
        data2 = response2.json()
        if data2['code'] == 0:
            songs2 = data2['data'].get('songs', [])
            print(f"SUCCESS (without status): Found {len(songs2)} charts")
        else:
            print(f"FAILED (without status): {data2['msg']}")
            
        return data
    except Exception as e:
        print(f"Error: {e}")
        return None

if __name__ == "__main__":
    test_get_chart(4)
    test_get_chart(6)
    test_get_all_charts()
    test_get_public_charts()
