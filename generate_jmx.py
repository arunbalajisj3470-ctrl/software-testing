#!/usr/bin/env python3
"""Generates one Apache JMeter test plan (.jmx) per JMeter question (Q46-Q60).

Run:  python3 generate_jmx.py
Each output file opens directly in JMeter (File > Open). Change BASE_HOST and the
paths to point at the real system under test before running.
"""
import os
from xml.sax.saxutils import escape

BASE_HOST = "example-target.test"   # <-- replace with the real host
BASE_PORT = "443"
PROTOCOL = "https"
OUT_DIR = os.path.dirname(os.path.abspath(__file__))

HEADER_JSON = [("Content-Type", "application/json")]


def http_sampler(name, method="GET", path="/", body=None, headers=None, assert_code="200"):
    headers = headers or []
    body_xml = ""
    if body is not None:
        body_xml = f"""
          <boolProp name="HTTPSampler.postBodyRaw">true</boolProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{escape(body)}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>"""
    header_xml = ""
    if headers:
        rows = "".join(
            f"""
            <elementProp name="" elementType="Header">
              <stringProp name="Header.name">{k}</stringProp>
              <stringProp name="Header.value">{escape(v)}</stringProp>
            </elementProp>""" for k, v in headers)
        header_xml = f"""
          <hashTree>
            <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="Headers" enabled="true">
              <collectionProp name="HeaderManager.headers">{rows}
              </collectionProp>
            </HeaderManager>
            <hashTree/>
          </hashTree>"""
    return f"""
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="{escape(name)}" enabled="true">
          <stringProp name="HTTPSampler.path">{escape(path)}</stringProp>
          <stringProp name="HTTPSampler.method">{method}</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>{body_xml}
        </HTTPSamplerProxy>
        <hashTree>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Status {assert_code}" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="{assert_code}">{assert_code}</stringProp>
            </collectionProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <intProp name="Assertion.test_type">8</intProp>
          </ResponseAssertion>
          <hashTree/>{header_xml}
        </hashTree>"""


def build_plan(spec):
    """spec keys: title, threads, ramp, duration (s) or loops, samplers, csv (optional), cookies (bool)."""
    loops = spec.get("loops", -1)
    duration = spec.get("duration", 60)
    scheduler = "true" if duration else "false"
    samplers = "".join(http_sampler(**s) for s in spec["samplers"])

    csv_xml, csv_tree = "", ""
    if spec.get("csv"):
        csv_xml = f"""
        <CSVDataSet guiclass="TestBeanGUI" testclass="CSVDataSet" testname="User Credentials CSV" enabled="true">
          <stringProp name="filename">{spec['csv']}</stringProp>
          <stringProp name="variableNames">username,password</stringProp>
          <stringProp name="delimiter">,</stringProp>
          <boolProp name="recycle">true</boolProp>
          <boolProp name="stopThread">false</boolProp>
          <boolProp name="ignoreFirstLine">true</boolProp>
        </CSVDataSet>
        <hashTree/>"""
    cookie_xml = ""
    if spec.get("cookies"):
        cookie_xml = """
        <CookieManager guiclass="CookiePanel" testclass="CookieManager" testname="HTTP Cookie Manager" enabled="true">
          <boolProp name="CookieManager.clearEachIteration">false</boolProp>
        </CookieManager>
        <hashTree/>"""

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="{escape(spec['title'])}" enabled="true">
      <stringProp name="TestPlan.comments">{escape(spec.get('comment', ''))}</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP Request Defaults" enabled="true">
        <stringProp name="HTTPSampler.domain">{BASE_HOST}</stringProp>
        <stringProp name="HTTPSampler.port">{BASE_PORT}</stringProp>
        <stringProp name="HTTPSampler.protocol">{PROTOCOL}</stringProp>
      </ConfigTestElement>
      <hashTree/>{csv_xml}{cookie_xml}
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Users" enabled="true">
        <stringProp name="ThreadGroup.num_threads">{spec['threads']}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">{spec['ramp']}</stringProp>
        <boolProp name="ThreadGroup.scheduler">{scheduler}</boolProp>
        <stringProp name="ThreadGroup.duration">{duration}</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <stringProp name="LoopController.loops">{loops}</stringProp>
          <boolProp name="LoopController.continue_forever">false</boolProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>{samplers}
        <hashTree/>
        <ResultCollector guiclass="SummaryReport" testclass="ResultCollector" testname="Summary Report" enabled="true">
          <boolProp name="ResultCollector.error_logging">false</boolProp>
        </ResultCollector>
        <hashTree/>
        <ResultCollector guiclass="AggregateReportGui" testclass="ResultCollector" testname="Aggregate Report" enabled="true">
          <boolProp name="ResultCollector.error_logging">false</boolProp>
        </ResultCollector>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
"""


SPECS = {
    "Q46_load_homepage_500_users": dict(
        title="Q46 Load test - e-commerce homepage, 500 users", threads=500, ramp=60, duration=300,
        comment="Festival sale: 500 concurrent users on homepage. Measure response time, throughput, error rate.",
        samplers=[dict(name="Homepage", path="/")]),

    "Q47_balance_enquiry_api": dict(
        title="Q47 Mobile banking balance enquiry API", threads=50, ramp=20, loops=20,
        comment="Balance enquiry REST API; verify status codes, response data and response time.",
        samplers=[dict(name="Balance Enquiry", method="GET", path="/api/v1/accounts/balance",
                       headers=[("Authorization", "Bearer ${token}")])]),

    "Q48_register_post_json": dict(
        title="Q48 User registration POST with JSON - valid and invalid", threads=200, ramp=30, duration=180,
        comment="Valid payload expects 201; invalid payload expects 400. Split into two samplers.",
        samplers=[
            dict(name="Register - valid", method="POST", path="/api/users",
                 body='{"name":"Test User","email":"user${__Random(1,100000)}@mail.test","password":"Abc@1234"}',
                 headers=HEADER_JSON, assert_code="201"),
            dict(name="Register - invalid", method="POST", path="/api/users",
                 body='{"name":"","email":"not-an-email","password":"x"}',
                 headers=HEADER_JSON, assert_code="400"),
        ]),

    "Q49_student_portal_login_results": dict(
        title="Q49 Student portal login - exam results burst", threads=500, ramp=60, duration=240,
        comment="Hundreds of students log in simultaneously and open exam results; check server stability.",
        samplers=[
            dict(name="Login", method="POST", path="/portal/login",
                 body='{"rollNo":"${rollNo}","password":"${pwd}"}', headers=HEADER_JSON),
            dict(name="View Results", path="/portal/results"),
        ]),

    "Q50_smoke_and_load_gov_site": dict(
        title="Q50 Smoke and load - government service website", threads=20, ramp=10, loops=5,
        comment="Smoke: 1 user, critical pages. Load: this 20-user run covers the same pages.",
        samplers=[
            dict(name="Home", path="/"),
            dict(name="Services", path="/services"),
            dict(name="Application Status", path="/status"),
            dict(name="Contact", path="/contact"),
        ]),

    "Q51_stress_result_publishing": dict(
        title="Q51 Stress test - result-publishing server (ramping users)", threads=1000, ramp=600, duration=900,
        comment="Ramp from 0 to 1000 users over 10 minutes to find the breaking point. Watch error % climb.",
        samplers=[dict(name="Results Page", path="/results/published")]),

    "Q52_payment_gateway_continuous": dict(
        title="Q52 Payment gateway - continuous transaction load", threads=300, ramp=60, duration=1800,
        comment="Continuous load for 30 minutes; analyze response time, throughput and error percentage.",
        samplers=[dict(name="Create Payment", method="POST", path="/api/payments",
                       body='{"amount":499.00,"currency":"INR","orderId":"ORD-${__UUID()}"}',
                       headers=HEADER_JSON, assert_code="200")]),

    "Q53_data_driven_login_csv": dict(
        title="Q53 Data-driven login API using CSV credentials", threads=100, ramp=30, loops=1,
        csv="users.csv",
        comment="users.csv has columns username,password; recycled per thread. Validates authentication responses.",
        samplers=[dict(name="Login with CSV", method="POST", path="/api/auth/login",
                       body='{"username":"${username}","password":"${password}"}',
                       headers=HEADER_JSON, assert_code="200")]),

    "Q54_session_cookie_shopping_cart": dict(
        title="Q54 Session and cookie handling - shopping cart", threads=200, ramp=40, duration=300,
        cookies=True,
        comment="Cookie Manager keeps the session cookie across requests so the cart persists.",
        samplers=[
            dict(name="Login", method="POST", path="/login",
                 body='{"user":"${user}","pass":"${pass}"}', headers=HEADER_JSON),
            dict(name="Browse", path="/products"),
            dict(name="View Cart", path="/cart"),
        ]),

    "Q55_end_to_end_checkout_flow": dict(
        title="Q55 End-to-end flow - login, browse, cart, checkout, logout", threads=150, ramp=60, duration=600,
        cookies=True,
        comment="Full user journey in one thread group, cookies kept between steps.",
        samplers=[
            dict(name="1 Login", method="POST", path="/login",
                 body='{"user":"${user}","pass":"${pass}"}', headers=HEADER_JSON),
            dict(name="2 Browse Products", path="/products"),
            dict(name="3 Add To Cart", method="POST", path="/cart/add",
                 body='{"productId":"P1001","qty":1}', headers=HEADER_JSON),
            dict(name="4 Checkout", method="POST", path="/checkout",
                 body='{"paymentMode":"UPI"}', headers=HEADER_JSON),
            dict(name="5 Logout", path="/logout"),
        ]),

    "Q56_capacity_movie_booking": dict(
        title="Q56 Capacity test - movie ticket booking (gradual ramp)", threads=800, ramp=900, duration=1200,
        comment="Peak booking: ramp users in steps; find max supported users and degradation point.",
        samplers=[dict(name="Seat Availability", path="/api/shows/12/seats")]),

    "Q57_ecommerce_search_perf": dict(
        title="Q57 E-commerce keyword search performance", threads=300, ramp=60, duration=300,
        comment="Keyword searches with random terms; average response time, throughput, error %.",
        samplers=[dict(name="Search", path="/search?q=${__RandomString(5,abcdefghijklmnopqrstuvwxyz)}")]),

    "Q58_endurance_lms_video_soak": dict(
        title="Q58 Endurance (soak) test - LMS video lectures", threads=200, ramp=120, duration=7200,
        comment="Constant load for 2 hours. Check response consistency, error rate and memory trend on server.",
        samplers=[dict(name="Video Lecture", path="/lectures/video/101/stream")]),

    "Q59_file_upload_download_cloud": dict(
        title="Q59 Cloud storage - concurrent upload and download", threads=100, ramp=30, loops=10,
        comment="Upload and download files of varying sizes; measure transfer rate and response time.",
        samplers=[
            dict(name="Upload 1MB", method="POST", path="/api/files/upload?size=1mb"),
            dict(name="Download 1MB", path="/api/files/download/sample-1mb.bin"),
            dict(name="Upload 10MB", method="POST", path="/api/files/upload?size=10mb"),
            dict(name="Download 10MB", path="/api/files/download/sample-10mb.bin"),
        ]),

    "Q60_weather_api_microservices": dict(
        title="Q60 Weather microservice API - high-frequency GET", threads=500, ramp=60, duration=300,
        comment="High-frequency GET requests under peak load; latency, throughput and failure rate.",
        samplers=[dict(name="Current Weather", path="/api/weather/current?city=Chennai")]),
}


def main():
    for name, spec in SPECS.items():
        path = os.path.join(OUT_DIR, f"{name}.jmx")
        with open(path, "w", encoding="utf-8") as f:
            f.write(build_plan(spec))
        print("wrote", path)


if __name__ == "__main__":
    main()
